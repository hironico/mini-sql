package net.hironico.minisql.utils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import net.hironico.minisql.DbConfig;

import java.util.regex.Pattern;

/**
 * Helper methods shared by all MongoDB related features (query execution, schema explorer...).
 */
public final class MongoUtils {

    private static final Pattern SIMPLE_COLLECTION_NAME = Pattern.compile("^\\w+$");

    private MongoUtils() {
    }

    /**
     * Builds the MongoDB connection string, injecting separate user/password credentials
     * into the URI when they are not already embedded in it.
     *
     * @param config the database configuration holding the MongoDB connection string
     * @return the connection string to use with the MongoDB driver
     */
    public static String buildConnectionString(DbConfig config) {
        String connStr = config.jdbcUrl == null ? "" : config.jdbcUrl.trim();
        String user = config.user == null ? "" : config.user.trim();
        String password = config.password == null ? "" : DbConfig.decryptPassword(config.password).trim();

        // Inject credentials only when they are provided separately and not already in the URI
        if (!user.isEmpty() && !connStr.contains("@")) {
            String encodedUser = user.replace("@", "%40");
            String encodedPassword = password.replace("@", "%40");
            String credentials = encodedPassword.isEmpty() ? encodedUser : encodedUser + ":" + encodedPassword;
            if (connStr.startsWith("mongodb+srv://")) {
                connStr = "mongodb+srv://" + credentials + "@" + connStr.substring("mongodb+srv://".length());
            } else if (connStr.startsWith("mongodb://")) {
                connStr = "mongodb://" + credentials + "@" + connStr.substring("mongodb://".length());
            }
        }
        return connStr;
    }

    /**
     * Creates a new {@link MongoClient} for the given configuration. Caller must close it.
     *
     * @param config the database configuration holding the MongoDB connection string
     * @return a new MongoClient
     */
    public static MongoClient createClient(DbConfig config) {
        ConnectionString connectionString = new ConnectionString(buildConnectionString(config));
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(settings);
    }

    /**
     * Returns the database name declared in the connection URL of the given configuration.
     *
     * @param config the database configuration holding the MongoDB connection string
     * @return the database name, or null when the URL does not specify any database or is invalid
     */
    public static String getDatabaseName(DbConfig config) {
        if (config == null || config.jdbcUrl == null || config.jdbcUrl.isBlank()) {
            return null;
        }
        try {
            String dbName = new ConnectionString(config.jdbcUrl.trim()).getDatabase();
            return dbName == null || dbName.isEmpty() ? null : dbName;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Returns the mongosh expression to access the given collection: {@code db.name} when the name is a
     * plain identifier, {@code db.getCollection("name")} otherwise (names containing dots, dashes...).
     *
     * @param collectionName the collection name
     * @return the mongosh expression referencing the collection
     */
    public static String getCollectionExpression(String collectionName) {
        if (SIMPLE_COLLECTION_NAME.matcher(collectionName).matches()) {
            return "db." + collectionName;
        }
        String escaped = collectionName.replace("\\", "\\\\").replace("\"", "\\\"");
        return "db.getCollection(\"" + escaped + "\")";
    }
}
