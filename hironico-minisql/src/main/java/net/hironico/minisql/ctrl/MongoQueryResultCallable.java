package net.hironico.minisql.ctrl;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.model.SQLResultSetTableModel;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Callable implementation that executes MongoDB queries using mongosh-style syntax
 * and returns results as {@link SQLResultSetTableModel} objects.
 *
 * <p>Supported commands:</p>
 * <ul>
 *   <li>{@code show dbs} / {@code show databases} — list all databases</li>
 *   <li>{@code show collections} — list collections in the current database</li>
 *   <li>{@code db.collection.find({filter}, {projection})} — query documents</li>
 *   <li>{@code db.collection.findOne({filter})} — query first matching document</li>
 *   <li>{@code db.collection.insertOne({doc})} — insert a document</li>
 *   <li>{@code db.collection.insertMany([{doc1},{doc2}])} — insert multiple documents</li>
 *   <li>{@code db.collection.updateOne({filter}, {update})} — update one document</li>
 *   <li>{@code db.collection.updateMany({filter}, {update})} — update many documents</li>
 *   <li>{@code db.collection.deleteOne({filter})} — delete one document</li>
 *   <li>{@code db.collection.deleteMany({filter})} — delete many documents</li>
 *   <li>{@code db.collection.countDocuments({filter})} — count matching documents</li>
 *   <li>{@code db.collection.aggregate([{stage1},{stage2}])} — aggregation pipeline</li>
 *   <li>{@code db.collection.drop()} — drop a collection</li>
 *   <li>{@code db.createCollection("name")} — create a collection</li>
 *   <li>{@code db.runCommand({cmd: 1})} — run an arbitrary command against the current database</li>
 *   <li>{@code db.adminCommand({cmd: 1})} — run an arbitrary command against the admin database</li>
 *   <li>{@code db.getCollectionNames()} — list collections (alternative to show collections)</li>
 *   <li>{@code db.stats()} — show database statistics</li>
 * </ul>
 *
 * <p>The MongoDB connection string is taken from {@link DbConfig#jdbcUrl}.
 * Credentials may be embedded in the URI ({@code mongodb://user:pass@host/db})
 * or provided separately via {@link DbConfig#user} and {@link DbConfig#password}.</p>
 */
public class MongoQueryResultCallable implements Callable<List<SQLResultSetTableModel>> {

    private static final Logger LOGGER = Logger.getLogger(MongoQueryResultCallable.class.getName());

    /** Matches: show dbs | show databases | show collections */
    private static final Pattern SHOW_CMD = Pattern.compile(
            "^\\s*show\\s+(\\w+)\\s*$", Pattern.CASE_INSENSITIVE);

    /** Matches: use <dbname> */
    private static final Pattern USE_CMD = Pattern.compile(
            "^\\s*use\\s+(\\w+)\\s*$", Pattern.CASE_INSENSITIVE);

    /** Matches: db.createCollection("name") or db.createCollection('name') */
    private static final Pattern CREATE_COLLECTION_CMD = Pattern.compile(
            "^\\s*db\\.createCollection\\([\"']([\\w]+)[\"']\\)\\s*;?\\s*$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches direct database-level method calls: db.method(args).
     * Used for db.runCommand(), db.adminCommand(), db.getCollectionNames(), etc.
     * This pattern is checked BEFORE COLLECTION_CMD so that two-level calls like
     * db.runCommand() are not confused with collection operations.
     */
    private static final Pattern DB_METHOD_CMD = Pattern.compile(
            "^\\s*db\\.([\\w]+)\\((.*)\\)\\s*;?\\s*$",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** Matches: db.<collection>.<method>(<args>) — the core pattern for collection operations */
    private static final Pattern COLLECTION_CMD = Pattern.compile(
            "^\\s*db\\.([\\w]+)\\.([\\w]+)\\((.*)\\)\\s*;?\\s*$",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private final String query;
    private final DbConfig config;

    /**
     * Constructs a new MongoQueryResultCallable.
     *
     * @param query  the mongosh-style query string to execute
     * @param config the database configuration holding the MongoDB connection string
     */
    public MongoQueryResultCallable(String query, DbConfig config) {
        this.query = query;
        this.config = config;
    }

    /**
     * Builds the MongoDB connection string, injecting separate user/password credentials
     * into the URI when they are not already embedded in it.
     */
    private String buildConnectionString() {
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

    @Override
    public List<SQLResultSetTableModel> call() throws Exception {
        List<SQLResultSetTableModel> results = new ArrayList<>();

        String connStr = buildConnectionString();
        ConnectionString connectionString = new ConnectionString(connStr);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        try (MongoClient client = MongoClients.create(settings)) {

            String dbName = connectionString.getDatabase();
            MongoDatabase db = (dbName != null && !dbName.isEmpty()) ? client.getDatabase(dbName) : null;

            String trimmed = query == null ? "" : query.trim();

            // --- show dbs / show databases / show collections ---
            Matcher showMatcher = SHOW_CMD.matcher(trimmed);
            if (showMatcher.matches()) {
                String what = showMatcher.group(1).toLowerCase();
                if ("dbs".equals(what) || "databases".equals(what)) {
                    results.add(executeShowDbs(client));
                } else if ("collections".equals(what)) {
                    requireDatabase(db);
                    results.add(executeShowCollections(db));
                } else {
                    throw new Exception("Unknown show command: show " + what
                            + "\nSupported: show dbs, show databases, show collections");
                }
                return results;
            }

            // --- use <dbname> ---
            Matcher useMatcher = USE_CMD.matcher(trimmed);
            if (useMatcher.matches()) {
                String newDb = useMatcher.group(1);
                SQLResultSetTableModel model = new SQLResultSetTableModel(
                        "use", trimmed, SQLResultSetTableModel.DISPLAY_TYPE_TABLE, "Message");
                model.addRow(new Object[]{
                        "Switched to database: " + newDb
                        + ". Please update the connection URL to use this database permanently."});
                results.add(model);
                return results;
            }

            // --- db.createCollection("name") ---
            Matcher createMatcher = CREATE_COLLECTION_CMD.matcher(trimmed);
            if (createMatcher.matches()) {
                requireDatabase(db);
                String collName = createMatcher.group(1);
                db.createCollection(collName);
                SQLResultSetTableModel model = new SQLResultSetTableModel(
                        "createCollection", trimmed, SQLResultSetTableModel.DISPLAY_TYPE_TABLE, "Message");
                model.addRow(new Object[]{"Collection '" + collName + "' created successfully."});
                results.add(model);
                return results;
            }

            // --- db.<method>(<args>) — database-level commands: runCommand, adminCommand, etc.
            // This MUST be checked before COLLECTION_CMD because COLLECTION_CMD is more specific
            // (three-part path db.col.method) and won't match two-part db.method calls.
            Matcher dbMethodMatcher = DB_METHOD_CMD.matcher(trimmed);
            if (dbMethodMatcher.matches()) {
                String method = dbMethodMatcher.group(1).toLowerCase();
                String argsStr = dbMethodMatcher.group(2).trim();
                SQLResultSetTableModel result = executeDbMethod(client, db, method, argsStr, trimmed);
                if (result != null) {
                    results.add(result);
                }
                return results;
            }

            // --- db.<collection>.<method>(<args>) ---
            Matcher collMatcher = COLLECTION_CMD.matcher(trimmed);
            if (collMatcher.matches()) {
                requireDatabase(db);
                String collectionName = collMatcher.group(1);
                String method = collMatcher.group(2).toLowerCase();
                String argsStr = collMatcher.group(3).trim();
                MongoCollection<Document> collection = db.getCollection(collectionName);
                SQLResultSetTableModel result = executeCollectionMethod(collection, method, argsStr, trimmed);
                if (result != null) {
                    results.add(result);
                }
                return results;
            }

            throw new Exception("Unrecognized MongoDB command:\n" + trimmed
                    + "\n\nSupported commands:\n"
                    + "  show dbs | show databases | show collections\n"
                    + "  db.runCommand({...}) | db.adminCommand({...})\n"
                    + "  db.getCollectionNames() | db.stats()\n"
                    + "  db.collection.find({filter}, {projection})\n"
                    + "  db.collection.findOne({filter})\n"
                    + "  db.collection.insertOne({doc}) | db.collection.insertMany([...])\n"
                    + "  db.collection.updateOne({filter},{update}) | db.collection.updateMany({filter},{update})\n"
                    + "  db.collection.deleteOne({filter}) | db.collection.deleteMany({filter})\n"
                    + "  db.collection.countDocuments({filter})\n"
                    + "  db.collection.aggregate([{stage1},...]) | db.collection.drop()\n"
                    + "  db.createCollection(\"name\")");
        }
    }

    // -------------------------------------------------------------------------
    // Helper: ensure a database is selected
    // -------------------------------------------------------------------------

    private void requireDatabase(MongoDatabase db) throws Exception {
        if (db == null) {
            throw new Exception(
                    "No database selected. Include the database name in the connection URL, "
                    + "e.g. mongodb://localhost:27017/myDatabase");
        }
    }

    // -------------------------------------------------------------------------
    // show dbs / show collections
    // -------------------------------------------------------------------------

    private SQLResultSetTableModel executeShowDbs(MongoClient client) {
        SQLResultSetTableModel model = new SQLResultSetTableModel(
                "Databases", "show dbs", SQLResultSetTableModel.DISPLAY_TYPE_TABLE,
                "name", "sizeOnDisk", "empty");
        List<Document> dbs = new ArrayList<>();
        client.listDatabases().into(dbs);
        for (Document d : dbs) {
            model.addRow(new Object[]{d.getString("name"), d.get("sizeOnDisk"), d.getBoolean("empty", false)});
        }
        return model;
    }

    private SQLResultSetTableModel executeShowCollections(MongoDatabase db) {
        SQLResultSetTableModel model = new SQLResultSetTableModel(
                "Collections", "show collections", SQLResultSetTableModel.DISPLAY_TYPE_TABLE,
                "collection_name");
        List<String> cols = new ArrayList<>();
        db.listCollectionNames().into(cols);
        Collections.sort(cols);
        for (String c : cols) {
            model.addRow(new Object[]{c});
        }
        return model;
    }

    // -------------------------------------------------------------------------
    // Collection method dispatcher
    // -------------------------------------------------------------------------

    private SQLResultSetTableModel executeCollectionMethod(MongoCollection<Document> collection,
                                                           String method, String argsStr,
                                                           String originalQuery) throws Exception {
        return switch (method) {
            case "find"            -> executeFind(collection, argsStr, originalQuery, false);
            case "findone"         -> executeFind(collection, argsStr, originalQuery, true);
            case "insertone"       -> executeInsertOne(collection, argsStr, originalQuery);
            case "insertmany"      -> executeInsertMany(collection, argsStr, originalQuery);
            case "updateone"       -> executeUpdate(collection, argsStr, originalQuery, false);
            case "updatemany"      -> executeUpdate(collection, argsStr, originalQuery, true);
            case "deleteone"       -> executeDelete(collection, argsStr, originalQuery, false);
            case "deletemany"      -> executeDelete(collection, argsStr, originalQuery, true);
            case "countdocuments"  -> executeCount(collection, argsStr, originalQuery);
            case "aggregate"       -> executeAggregate(collection, argsStr, originalQuery);
            case "drop"            -> executeDrop(collection, originalQuery);
            default -> throw new Exception("Unsupported collection method: " + method
                    + "\nSupported: find, findOne, insertOne, insertMany, updateOne, updateMany, "
                    + "deleteOne, deleteMany, countDocuments, aggregate, drop");
        };
    }

    // -------------------------------------------------------------------------
    // find / findOne
    // -------------------------------------------------------------------------

    private SQLResultSetTableModel executeFind(MongoCollection<Document> collection,
                                               String argsStr, String originalQuery,
                                               boolean findOne) throws Exception {
        Document filter = new Document();
        Document projection = null;

        if (!argsStr.isEmpty()) {
            List<String> args = splitTopLevelArgs(argsStr);
            if (!args.isEmpty() && !args.get(0).trim().isEmpty()) {
                filter = Document.parse(args.get(0).trim());
            }
            if (args.size() > 1 && !args.get(1).trim().isEmpty()) {
                projection = Document.parse(args.get(1).trim());
            }
        }

        FindIterable<Document> iterable = collection.find(filter);
        if (projection != null) {
            iterable = iterable.projection(projection);
        }
        if (findOne) {
            iterable = iterable.limit(1);
        }

        List<Document> docs = new ArrayList<>();
        iterable.into(docs);
        return documentsToTableModel(docs, findOne ? "findOne" : "find", originalQuery);
    }

    // -------------------------------------------------------------------------
    // insertOne / insertMany
    // -------------------------------------------------------------------------

    private SQLResultSetTableModel executeInsertOne(MongoCollection<Document> collection,
                                                    String argsStr, String originalQuery) throws Exception {
        if (argsStr.isEmpty()) {
            throw new Exception("insertOne requires a document argument, e.g. db.col.insertOne({name: \"Alice\"})");
        }
        Document doc = Document.parse(argsStr.trim());
        collection.insertOne(doc);

        SQLResultSetTableModel model = new SQLResultSetTableModel(
                "insertOne", originalQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE,
                "acknowledged", "insertedId");
        Object id = doc.get("_id");
        model.addRow(new Object[]{true, id != null ? id.toString() : "generated"});
        return model;
    }

    private SQLResultSetTableModel executeInsertMany(MongoCollection<Document> collection,
                                                     String argsStr, String originalQuery) throws Exception {
        if (argsStr.isEmpty()) {
            throw new Exception("insertMany requires an array of documents, e.g. db.col.insertMany([{a:1},{a:2}])");
        }
        Document wrapper = Document.parse("{\"docs\":" + argsStr.trim() + "}");
        List<Document> docs = wrapper.getList("docs", Document.class);
        collection.insertMany(docs);

        SQLResultSetTableModel model = new SQLResultSetTableModel(
                "insertMany", originalQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE,
                "acknowledged", "insertedCount");
        model.addRow(new Object[]{true, docs.size()});
        return model;
    }

    // -------------------------------------------------------------------------
    // updateOne / updateMany
    // -------------------------------------------------------------------------

    private SQLResultSetTableModel executeUpdate(MongoCollection<Document> collection,
                                                 String argsStr, String originalQuery,
                                                 boolean many) throws Exception {
        List<String> args = splitTopLevelArgs(argsStr);
        if (args.size() < 2) {
            String cmd = many ? "updateMany" : "updateOne";
            throw new Exception(cmd + " requires two arguments: db.col." + cmd + "({filter}, {update})");
        }
        Document filter = Document.parse(args.get(0).trim());
        Document update = Document.parse(args.get(1).trim());
        long modified = many
                ? collection.updateMany(filter, update).getModifiedCount()
                : collection.updateOne(filter, update).getModifiedCount();

        String title = many ? "updateMany" : "updateOne";
        SQLResultSetTableModel model = new SQLResultSetTableModel(
                title, originalQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE,
                "acknowledged", "modifiedCount");
        model.addRow(new Object[]{true, modified});
        return model;
    }

    // -------------------------------------------------------------------------
    // deleteOne / deleteMany
    // -------------------------------------------------------------------------

    private SQLResultSetTableModel executeDelete(MongoCollection<Document> collection,
                                                 String argsStr, String originalQuery,
                                                 boolean many) throws Exception {
        Document filter = argsStr.isEmpty() ? new Document() : Document.parse(argsStr.trim());
        long deleted = many
                ? collection.deleteMany(filter).getDeletedCount()
                : collection.deleteOne(filter).getDeletedCount();

        String title = many ? "deleteMany" : "deleteOne";
        SQLResultSetTableModel model = new SQLResultSetTableModel(
                title, originalQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE,
                "acknowledged", "deletedCount");
        model.addRow(new Object[]{true, deleted});
        return model;
    }

    // -------------------------------------------------------------------------
    // countDocuments
    // -------------------------------------------------------------------------

    private SQLResultSetTableModel executeCount(MongoCollection<Document> collection,
                                                String argsStr, String originalQuery) {
        Document filter = (argsStr.isEmpty() || "{}".equals(argsStr.trim()))
                ? new Document() : Document.parse(argsStr.trim());
        long count = collection.countDocuments(filter);

        SQLResultSetTableModel model = new SQLResultSetTableModel(
                "countDocuments", originalQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE,
                "count");
        model.addRow(new Object[]{count});
        return model;
    }

    // -------------------------------------------------------------------------
    // aggregate
    // -------------------------------------------------------------------------

    private SQLResultSetTableModel executeAggregate(MongoCollection<Document> collection,
                                                    String argsStr, String originalQuery) throws Exception {
        if (argsStr.isEmpty()) {
            throw new Exception("aggregate requires a pipeline array, e.g. db.col.aggregate([{$match:{a:1}}])");
        }
        Document wrapper = Document.parse("{\"pipeline\":" + argsStr.trim() + "}");
        List<Document> pipeline = wrapper.getList("pipeline", Document.class);

        List<Document> results = new ArrayList<>();
        collection.aggregate(pipeline).into(results);
        return documentsToTableModel(results, "aggregate", originalQuery);
    }

    // -------------------------------------------------------------------------
    // drop
    // -------------------------------------------------------------------------

    private SQLResultSetTableModel executeDrop(MongoCollection<Document> collection,
                                               String originalQuery) {
        collection.drop();
        SQLResultSetTableModel model = new SQLResultSetTableModel(
                "drop", originalQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE, "Message");
        model.addRow(new Object[]{"Collection dropped successfully."});
        return model;
    }

    // -------------------------------------------------------------------------
    // Direct database-level commands (db.method(...))
    // -------------------------------------------------------------------------

    /**
     * Dispatches direct database-level commands such as {@code db.runCommand()},
     * {@code db.adminCommand()}, {@code db.stats()}, and {@code db.getCollectionNames()}.
     *
     * @param client        the active MongoClient
     * @param db            the selected database (may be null if no database in the URL)
     * @param method        lower-cased method name (e.g. "runcommand", "adminccommand")
     * @param argsStr       raw argument string extracted from inside the parentheses
     * @param originalQuery the full original query string
     * @return a {@link SQLResultSetTableModel} containing the result
     * @throws Exception if the command cannot be executed
     */
    private SQLResultSetTableModel executeDbMethod(MongoClient client, MongoDatabase db,
                                                   String method, String argsStr,
                                                   String originalQuery) throws Exception {
        return switch (method) {
            case "runcommand" -> {
                requireDatabase(db);
                if (argsStr.isEmpty()) {
                    throw new Exception("runCommand requires a command document, e.g. db.runCommand({hello: 1})");
                }
                Document cmd = Document.parse(argsStr);
                Document result = db.runCommand(cmd);
                yield documentToKeyValueTableModel(result, "runCommand", originalQuery);
            }
            case "admincommand" -> {
                if (argsStr.isEmpty()) {
                    throw new Exception("adminCommand requires a command document, e.g. db.adminCommand({ping: 1})");
                }
                Document cmd = Document.parse(argsStr);
                Document result = client.getDatabase("admin").runCommand(cmd);
                yield documentToKeyValueTableModel(result, "adminCommand", originalQuery);
            }
            case "stats" -> {
                requireDatabase(db);
                Document result = db.runCommand(new Document("dbStats", 1));
                yield documentToKeyValueTableModel(result, "stats", originalQuery);
            }
            case "getcollectionnames" -> {
                requireDatabase(db);
                yield executeShowCollections(db);
            }
            case "getcollectioninfos" -> {
                requireDatabase(db);
                List<Document> infos = new ArrayList<>();
                db.listCollections().into(infos);
                yield documentsToTableModel(infos, "getCollectionInfos", originalQuery);
            }
            case "listcollections" -> {
                requireDatabase(db);
                List<Document> infos = new ArrayList<>();
                db.listCollections().into(infos);
                yield documentsToTableModel(infos, "listCollections", originalQuery);
            }
            case "createcollection" -> {
                // Handles db.createCollection("name") when name is passed without quotes in the DB_METHOD_CMD match
                requireDatabase(db);
                String collName = argsStr.trim().replaceAll("^[\"']|[\"']$", "");
                db.createCollection(collName);
                SQLResultSetTableModel model = new SQLResultSetTableModel(
                        "createCollection", originalQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE, "Message");
                model.addRow(new Object[]{"Collection '" + collName + "' created successfully."});
                yield model;
            }
            default -> throw new Exception("Unsupported database method: db." + method + "()\n"
                    + "Supported: db.runCommand({...}), db.adminCommand({...}), db.stats(), "
                    + "db.getCollectionNames(), db.getCollectionInfos(), db.listCollections(), "
                    + "db.createCollection(\"name\")");
        };
    }

    /**
     * Converts a single MongoDB {@link Document} into a key-value {@link SQLResultSetTableModel}
     * with columns {@code field} and {@code value}. Nested documents and arrays are rendered
     * as their JSON string representation.
     *
     * @param doc           the document to display
     * @param title         the display title for the result tab
     * @param originalQuery the originating query string
     * @return a table model with one row per top-level field in the document
     */
    private SQLResultSetTableModel documentToKeyValueTableModel(Document doc,
                                                                 String title,
                                                                 String originalQuery) {
        SQLResultSetTableModel model = new SQLResultSetTableModel(
                title, originalQuery, SQLResultSetTableModel.DISPLAY_TYPE_TABLE,
                "field", "value");
        if (doc == null) {
            model.addRow(new Object[]{"(no result)", ""});
            return model;
        }
        for (String key : doc.keySet()) {
            Object val = doc.get(key);
            String valStr = val == null ? "null" : val.toString();
            model.addRow(new Object[]{key, valStr});
        }
        return model;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a list of MongoDB {@link Document} objects into a {@link SQLResultSetTableModel}.
     * Column names are derived from the union of keys across all documents, preserving
     * insertion order. The {@code _id} field is always placed first when present.
     */
    private SQLResultSetTableModel documentsToTableModel(List<Document> docs,
                                                         String title, String query) {
        if (docs.isEmpty()) {
            SQLResultSetTableModel model = new SQLResultSetTableModel(
                    title, query, SQLResultSetTableModel.DISPLAY_TYPE_TABLE, "(no results)");
            return model;
        }

        // Collect all column names preserving order; _id first
        LinkedHashSet<String> colSet = new LinkedHashSet<>();
        for (Document doc : docs) {
            if (doc.containsKey("_id")) colSet.add("_id");
        }
        for (Document doc : docs) {
            colSet.addAll(doc.keySet());
        }
        String[] columns = colSet.toArray(new String[0]);

        SQLResultSetTableModel model = new SQLResultSetTableModel(
                title, query, SQLResultSetTableModel.DISPLAY_TYPE_TABLE, columns);

        for (Document doc : docs) {
            Object[] row = new Object[columns.length];
            for (int i = 0; i < columns.length; i++) {
                Object val = doc.get(columns[i]);
                row[i] = val == null ? null : val.toString();
            }
            model.addRow(row);
        }
        return model;
    }

    /**
     * Splits a comma-separated argument string at the top level, respecting
     * nested braces {@code {}}, brackets {@code []}, and parentheses {@code ()}.
     *
     * @param argsStr the raw argument string from inside the method call
     * @return list of individual argument strings, trimmed
     */
    private List<String> splitTopLevelArgs(String argsStr) {
        List<String> args = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < argsStr.length(); i++) {
            char c = argsStr.charAt(i);

            if (inString) {
                current.append(c);
                if (c == stringChar && (i == 0 || argsStr.charAt(i - 1) != '\\')) {
                    inString = false;
                }
            } else if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                current.append(c);
            } else if (c == '{' || c == '[' || c == '(') {
                depth++;
                current.append(c);
            } else if (c == '}' || c == ']' || c == ')') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                args.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            args.add(current.toString().trim());
        }
        return args;
    }
}
