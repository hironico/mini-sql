package ch.ubp.pms.minisql.ui.history;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "query-history-entry")
public class QueryHistoryEntry implements Comparable<QueryHistoryEntry> {

    @JacksonXmlProperty(localName = "timestamp", isAttribute = true)
    public Long timestamp = System.currentTimeMillis();

    @JacksonXmlProperty(localName = "query", isAttribute = true)
    public String query = "";

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (! (obj instanceof QueryHistoryEntry)) {
            return false;
        }

        QueryHistoryEntry other = (QueryHistoryEntry)obj;

        if (other.timestamp == null && this.timestamp == null) {
            return true;
        }

        if (other.timestamp == null || this.timestamp == null) {
            return false;
        }

        return other.timestamp.equals(this.timestamp);
    }

    @Override
    public String toString() {
        return query;
    }
    /**
     * Compare the timestmap of the entry
     * @param o other entry to compare to
     * @return inverse result of the natural compare to ensure most recent entries to be displayed on top
     */
    @Override
    public int compareTo(QueryHistoryEntry o) {
        if (o == null) {
            return 1;
        }

        if (this.timestamp == null && o.timestamp == null) {
            return 0;
        }

        if (this.timestamp != null) {
            return -1 * this.timestamp.compareTo(o.timestamp);
        } else {
            return 1;
        }
    }
}