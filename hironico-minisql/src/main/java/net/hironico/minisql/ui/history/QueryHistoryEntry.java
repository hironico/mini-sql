package net.hironico.minisql.ui.history;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Data object representing a single entry in the SQL query history.
 * Contains the SQL query text and execution timestamp, with XML serialization
 * support for persistence to disk. Implements natural ordering by timestamp
 * (most recent first).
 */
@JacksonXmlRootElement(localName = "query-history-entry")
public class QueryHistoryEntry implements Comparable<QueryHistoryEntry> {

    /** Execution timestamp in milliseconds since epoch */
    @JacksonXmlProperty(localName = "timestamp", isAttribute = true)
    public Long timestamp = System.currentTimeMillis();

    /** The SQL query text that was executed */
    @JacksonXmlProperty(localName = "query", isAttribute = true)
    public String query = "";

    /**
     * Checks equality based on timestamp values.
     * Two entries are considered equal if they have the same timestamp.
     *
     * @param obj the object to compare with this entry
     * @return true if the timestamps are equal, false otherwise
     */
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

    /**
     * Returns the SQL query text as the string representation.
     *
     * @return the SQL query string
     */
    @Override
    public String toString() {
        return query;
    }

    /**
     * Compares entries by timestamp for sorting (most recent first).
     * Implements reverse chronological ordering so that newer entries
     * appear at the beginning of sorted collections.
     *
     * @param o the other QueryHistoryEntry to compare to
     * @return negative if this entry is more recent, positive if older,
     *         zero if timestamps are equal
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
