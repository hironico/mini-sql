package net.hironico.minisql.ctrl;

import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.utils.MongoUtils;
import org.bson.Document;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestMongoCollections {

    private static Document info(String name, String type) {
        return new Document("name", name).append("type", type);
    }

    private final List<Document> infos = List.of(
            info("users", "collection"),
            info("system.views", "collection"),
            info("activeUsers", "view"),
            info("metrics", "timeseries"));

    @Test
    public void testToObjectType() {
        assertEquals(SQLObjectTypeEnum.COLLECTION, MongoCollectionListCallable.toObjectType("users", "collection"));
        assertEquals(SQLObjectTypeEnum.COLLECTION, MongoCollectionListCallable.toObjectType("metrics", "timeseries"));
        assertEquals(SQLObjectTypeEnum.VIEW, MongoCollectionListCallable.toObjectType("v", "view"));
        assertEquals(SQLObjectTypeEnum.SYSTEM_COLLECTION, MongoCollectionListCallable.toObjectType("system.views", "collection"));
    }

    @Test
    public void testToRowsWithoutFilterIsSorted() {
        List<String[]> rows = MongoCollectionListCallable.toRows("mydb", infos, null);
        assertEquals(4, rows.size());
        assertEquals("activeUsers", rows.get(0)[1]);
        assertEquals("metrics", rows.get(1)[1]);
        assertEquals("system.views", rows.get(2)[1]);
        assertEquals("users", rows.get(3)[1]);
        assertEquals("mydb", rows.get(0)[0]);
        assertEquals("VIEW", rows.get(0)[2]);
        assertEquals("SYSTEM COLLECTION", rows.get(2)[2]);
    }

    @Test
    public void testToRowsWithFilters() {
        List<String[]> collections = MongoCollectionListCallable.toRows("mydb", infos, SQLObjectTypeEnum.COLLECTION);
        assertEquals(3, collections.size());
        assertTrue(collections.stream().noneMatch(r -> "VIEW".equals(r[2])));

        List<String[]> views = MongoCollectionListCallable.toRows("mydb", infos, SQLObjectTypeEnum.VIEW);
        assertEquals(1, views.size());
        assertEquals("activeUsers", views.get(0)[1]);
    }

    @Test
    public void testGetCollectionCommandParsing() {
        Matcher m = MongoQueryResultCallable.GET_COLLECTION_CMD.matcher("db.getCollection(\"a.b-c\").find({\"x\": 1})");
        assertTrue(m.matches());
        assertEquals("a.b-c", m.group(2));
        assertEquals("find", m.group(3));
        assertEquals("{\"x\": 1}", m.group(4));

        m = MongoQueryResultCallable.GET_COLLECTION_CMD.matcher("db.getCollection('my coll').countDocuments({});");
        assertTrue(m.matches());
        assertEquals("my coll", m.group(2));
        assertEquals("countDocuments", m.group(3));

        assertFalse(MongoQueryResultCallable.GET_COLLECTION_CMD.matcher("db.users.find({})").matches());
    }

    @Test
    public void testGeneratedQueriesRoundTrip() {
        String name = "we\"ird.name-1";
        String query = MongoUtils.getCollectionExpression(name) + ".aggregate([{ \"$limit\": 1000 }])";
        Matcher m = MongoQueryResultCallable.GET_COLLECTION_CMD.matcher(query);
        assertTrue(m.matches());
        assertEquals(name, MongoQueryResultCallable.unescapeCollectionName(m.group(2)));
        assertEquals("aggregate", m.group(3));
        assertEquals("[{ \"$limit\": 1000 }]", m.group(4));
    }
}
