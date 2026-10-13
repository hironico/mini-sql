package net.hironico.minisql.utils;

import net.hironico.minisql.DbConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestMongoUtils {

    private static DbConfig config(String url, String user, String clearPassword) {
        DbConfig cfg = new DbConfig();
        cfg.jdbcUrl = url;
        cfg.user = user;
        cfg.password = clearPassword == null ? null : DbConfig.encryptPassword(clearPassword);
        cfg.type = DbConfig.CONNECTION_TYPE_MONGODB;
        return cfg;
    }

    @Test
    public void testBuildConnectionStringInjectsCredentials() {
        DbConfig cfg = config("mongodb://localhost:27017/mydb", "bob@corp", "s3cr@t");
        assertEquals("mongodb://bob%40corp:s3cr%40t@localhost:27017/mydb", MongoUtils.buildConnectionString(cfg));
    }

    @Test
    public void testBuildConnectionStringKeepsEmbeddedCredentials() {
        DbConfig cfg = config("mongodb://alice:pwd@localhost:27017/mydb", "bob", "other");
        assertEquals("mongodb://alice:pwd@localhost:27017/mydb", MongoUtils.buildConnectionString(cfg));
    }

    @Test
    public void testGetDatabaseName() {
        assertEquals("mydb", MongoUtils.getDatabaseName(config("mongodb://localhost:27017/mydb", null, null)));
        assertEquals("mydb", MongoUtils.getDatabaseName(config("mongodb://u:p@localhost/mydb?authSource=admin", null, null)));
        assertNull(MongoUtils.getDatabaseName(config("mongodb://localhost:27017", null, null)));
        assertNull(MongoUtils.getDatabaseName(config("mongodb://localhost:27017/", null, null)));
        assertNull(MongoUtils.getDatabaseName(config("not a mongo url", null, null)));
        assertNull(MongoUtils.getDatabaseName(config(null, null, null)));
        assertNull(MongoUtils.getDatabaseName(null));
    }

    @Test
    public void testGetCollectionExpression() {
        assertEquals("db.users", MongoUtils.getCollectionExpression("users"));
        assertEquals("db.getCollection(\"system.views\")", MongoUtils.getCollectionExpression("system.views"));
        assertEquals("db.getCollection(\"my-coll\")", MongoUtils.getCollectionExpression("my-coll"));
        assertEquals("db.getCollection(\"a\\\"b\")", MongoUtils.getCollectionExpression("a\"b"));
    }
}
