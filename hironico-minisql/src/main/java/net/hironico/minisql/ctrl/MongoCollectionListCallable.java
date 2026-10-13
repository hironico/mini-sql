package net.hironico.minisql.ctrl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.utils.MongoUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lists the collections and views of a MongoDB database. Results follow the same contract as
 * {@link ObjectListCallable}: each row is {@code [databaseName, objectName, objectType]} where
 * objectType is the string value of a {@link SQLObjectTypeEnum}.
 */
public class MongoCollectionListCallable implements Callable<List<String[]>>, Supplier<List<String[]>> {

    private static final Logger LOGGER = Logger.getLogger(MongoCollectionListCallable.class.getName());

    private final DbConfig configToUse;
    private final String databaseName;
    private final SQLObjectTypeEnum objectFilter;

    /**
     * @param configToUse  the MongoDB configuration
     * @param databaseName the database to list collections from
     * @param objectFilter {@link SQLObjectTypeEnum#COLLECTION}, {@link SQLObjectTypeEnum#VIEW} or null for all
     */
    public MongoCollectionListCallable(DbConfig configToUse, String databaseName, SQLObjectTypeEnum objectFilter) {
        this.configToUse = configToUse;
        this.databaseName = databaseName;
        this.objectFilter = objectFilter;
    }

    /**
     * Maps a {@code listCollections} entry type to a {@link SQLObjectTypeEnum}.
     *
     * @param name the collection name
     * @param mongoType the collection type as returned by MongoDB ("collection", "view", "timeseries"...)
     * @return VIEW for views, SYSTEM_COLLECTION for {@code system.*} collections, COLLECTION otherwise
     */
    public static SQLObjectTypeEnum toObjectType(String name, String mongoType) {
        if ("view".equalsIgnoreCase(mongoType)) {
            return SQLObjectTypeEnum.VIEW;
        }
        if (name != null && name.startsWith("system.")) {
            return SQLObjectTypeEnum.SYSTEM_COLLECTION;
        }
        return SQLObjectTypeEnum.COLLECTION;
    }

    /**
     * Converts {@code listCollections} documents into explorer rows, applying the object filter.
     */
    static List<String[]> toRows(String databaseName, List<Document> collectionInfos, SQLObjectTypeEnum objectFilter) {
        List<String[]> result = new ArrayList<>();
        for (Document info : collectionInfos) {
            String name = info.getString("name");
            if (name == null) {
                continue;
            }
            SQLObjectTypeEnum type = toObjectType(name, info.getString("type"));
            if (!matchesFilter(type, objectFilter)) {
                continue;
            }
            result.add(new String[]{databaseName, name, type.toString()});
        }
        result.sort(Comparator.comparing(row -> row[1]));
        return result;
    }

    private static boolean matchesFilter(SQLObjectTypeEnum type, SQLObjectTypeEnum objectFilter) {
        if (objectFilter == null) {
            return true;
        }
        if (SQLObjectTypeEnum.isCollection(objectFilter)) {
            return SQLObjectTypeEnum.isCollection(type);
        }
        return type == objectFilter;
    }

    @Override
    public List<String[]> call() {
        if (configToUse == null) {
            LOGGER.severe("Config to use is null. Cannot get collection list.");
            return null;
        }
        if (databaseName == null) {
            LOGGER.severe("Cannot get collection list for null database.");
            return null;
        }

        try (MongoClient client = MongoUtils.createClient(configToUse)) {
            MongoDatabase db = client.getDatabase(databaseName);
            List<Document> infos = new ArrayList<>();
            db.listCollections().into(infos);
            return toRows(databaseName, infos, objectFilter);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Error while getting the MongoDB collection list.", t);
            List<String[]> result = new ArrayList<>();
            result.add(new String[]{"Error while getting the collection list. See log.", t.getMessage(), ""});
            return result;
        }
    }

    @Override
    public List<String[]> get() {
        return call();
    }
}
