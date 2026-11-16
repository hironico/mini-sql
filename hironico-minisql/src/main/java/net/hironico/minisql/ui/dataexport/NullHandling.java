package net.hironico.minisql.ui.dataexport;

/**
 * What to write in a CSV file for a SQL NULL value. A NULL is never enclosed in quotes, so it can always
 * be told apart from an empty string.
 */
public enum NullHandling {
    /** the field is left empty */
    EMPTY("Leave empty"),
    /** the field contains the NULL literal */
    NULL_LITERAL("Write NULL");

    private final String label;

    NullHandling(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
