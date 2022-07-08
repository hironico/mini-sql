package ch.ubp.pms.minisql.ui.history;

import java.io.File;
import java.io.IOException;
import java.util.*;

import ch.ubp.pms.utils.XMLFile;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "query-history")
public class QueryHistoryFile {

    private static final String HISTORY_FILE = String.format("%s%s%s", System.getProperty("user.home"), File.separator, "minisql-history.xml");

    private static class QueryHistoryComparator implements Comparator<QueryHistoryEntry> {

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

    private final TreeSet<QueryHistoryEntry> sqlHistory = new TreeSet<>(new QueryHistoryComparator());


    public static QueryHistoryFile load() {
        try {
            return XMLFile.load(new File(HISTORY_FILE), QueryHistoryFile.class);
        } catch (IOException ioe) {
            return new QueryHistoryFile();
        }
    }

    public void save() {
        try {
            XMLFile.saveAs(new File(HISTORY_FILE), this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public QueryHistoryEntry add(String query) {
        QueryHistoryEntry entry = new QueryHistoryEntry();
        entry.query = query;
        sqlHistory.add(entry);
        this.save();
        return entry;
    }

    @JacksonXmlProperty(localName = "query-history-entry")
    public TreeSet<QueryHistoryEntry> getSqlHistory() {
        return this.sqlHistory;
    }
    
}