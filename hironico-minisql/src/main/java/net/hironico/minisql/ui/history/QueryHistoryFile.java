package net.hironico.minisql.ui.history;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.hironico.common.utils.XMLFile;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Manages the persistence and retrieval of SQL query history.
 * This class handles loading, saving, and managing query history entries stored in an XML file
 * in the user's home directory.
 */
@JacksonXmlRootElement(localName = "query-history")
public class QueryHistoryFile {

    /** Path to the query history XML file in user's home directory */
    private static final String HISTORY_FILE = String.format("%s%s%s", System.getProperty("user.home"), File.separator, "minisql-history.xml");

    /**
     * Comparator for sorting QueryHistoryEntry objects.
     * Implements natural ordering based on the QueryHistoryEntry's compareTo method.
     */
    private static class QueryHistoryComparator implements Comparator<QueryHistoryEntry> {

        /**
         * Compares two QueryHistoryEntry objects for ordering.
         * 
         * @param a the first QueryHistoryEntry
         * @param b the second QueryHistoryEntry
         * @return a negative integer, zero, or a positive integer as the first argument
         *         is less than, equal to, or greater than the second
         */
        @Override
        public int compare(QueryHistoryEntry a, QueryHistoryEntry b) {
            if (a == null && b == null) {
                return 0;
            }

            if (a == null) {
                return -1;
            }

            return a.compareTo(b);
        }
    }

    /** Sorted set of query history entries using the custom comparator */
    private final TreeSet<QueryHistoryEntry> sqlHistory = new TreeSet<>(new QueryHistoryComparator());

    /**
     * Loads the query history from the XML file.
     * If the file doesn't exist or cannot be read, returns a new empty history.
     * 
     * @return the loaded QueryHistoryFile, or a new empty one if loading fails
     */
    public static QueryHistoryFile load() {
        try {
            return XMLFile.load(new File(HISTORY_FILE), QueryHistoryFile.class);
        } catch (IOException ioe) {
            return new QueryHistoryFile();
        }
    }

    /**
     * Saves the query history to the XML file.
     * Prints stack trace if saving fails but doesn't throw exception.
     */
    public void save() {
        try {
            XMLFile.saveAs(new File(HISTORY_FILE), this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Adds a new query to the history and saves the updated history.
     * Creates a new QueryHistoryEntry with the specified query string and adds it to the sorted set.
     * 
     * @param query the SQL query string to add to the history
     * @return the newly created QueryHistoryEntry
     */
    public QueryHistoryEntry add(String query) {
        QueryHistoryEntry entry = new QueryHistoryEntry();
        entry.query = query;
        sqlHistory.add(entry);
        this.save();
        return entry;
    }

    /**
     * Gets the sorted set of SQL history entries.
     * 
     * @return the TreeSet containing all QueryHistoryEntry objects
     */
    @JacksonXmlProperty(localName = "query-history-entry")
    public TreeSet<QueryHistoryEntry> getSqlHistory() {
        return this.sqlHistory;
    }
    
}
