package net.hironico.minisql.ui.dbexplorer;

import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestSQLObjectsTreeTableModel {

    private static List<String> rootNames(SQLObjectsTreeTableModel model) {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) model.getRoot();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            names.add((String) ((DefaultMutableTreeTableNode) root.getChildAt(i)).getUserObject());
        }
        return names;
    }

    private static List<String> childNames(SQLObjectsTreeTableModel model, String rootName) {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) model.getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) root.getChildAt(i);
            if (rootName.equals(node.getUserObject())) {
                List<String> names = new ArrayList<>();
                for (int j = 0; j < node.getChildCount(); j++) {
                    names.add(((SQLObject) ((DefaultMutableTreeTableNode) node.getChildAt(j)).getUserObject()).name);
                }
                return names;
            }
        }
        throw new AssertionError("No root node " + rootName);
    }

    private static List<String[]> mongoRows() {
        return List.of(
                new String[]{"mydb", "users", "COLLECTION"},
                new String[]{"mydb", "system.views", "SYSTEM COLLECTION"},
                new String[]{"mydb", "activeUsers", "VIEW"});
    }

    @Test
    public void testDefaultLayoutIsRelational() {
        SQLObjectsTreeTableModel model = new SQLObjectsTreeTableModel();
        assertFalse(model.isMongoMode());
        assertTrue(rootNames(model).containsAll(List.of("Tables", "Views", "Procedures", "Sequences", "Enums")));
        assertEquals(5, rootNames(model).size());
    }

    @Test
    public void testMongoLayoutAndRouting() {
        SQLObjectsTreeTableModel model = new SQLObjectsTreeTableModel();
        model.setMongoMode(true);
        assertEquals(List.of("Collections", "Views"), rootNames(model));

        model.setSQLObjects(mongoRows());
        assertEquals(List.of("users"), childNames(model, "Collections"));
        assertEquals(List.of("activeUsers"), childNames(model, "Views"));

        model.clear(SQLObjectTypeEnum.COLLECTION);
        assertTrue(childNames(model, "Collections").isEmpty());
        assertEquals(List.of("activeUsers"), childNames(model, "Views"));
    }

    @Test
    public void testSystemCollectionsShownOnDemand() {
        SQLObjectsTreeTableModel model = new SQLObjectsTreeTableModel();
        model.setMongoMode(true);
        model.setShowSystemObjects(true);
        model.setSQLObjects(mongoRows());
        assertEquals(List.of("users", "system.views"), childNames(model, "Collections"));
    }

    @Test
    public void testSwitchBackToRelational() {
        SQLObjectsTreeTableModel model = new SQLObjectsTreeTableModel();
        model.setMongoMode(true);
        model.setSQLObjects(mongoRows());
        model.setMongoMode(false);
        assertEquals(5, rootNames(model).size());
        assertFalse(rootNames(model).contains("Collections"));

        model.setSQLObjects(List.<String[]>of(new String[]{"public", "t1", "TABLE"}, new String[]{"mydb", "c1", "COLLECTION"}));
        assertEquals(List.of("t1"), childNames(model, "Tables"));
        model.clear();
        assertTrue(childNames(model, "Tables").isEmpty());
    }
}
