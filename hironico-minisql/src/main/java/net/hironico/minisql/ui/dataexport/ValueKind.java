package net.hironico.minisql.ui.dataexport;

/**
 * Kind of a value handed to a {@link RowWriter}. Lets the writers pick the right literal syntax.
 */
public enum ValueKind {
    /** SQL NULL, the text is null */
    NULL,
    /** numeric value, the text is the plain number */
    NUMBER,
    /** boolean value, the text is "true" or "false" */
    BOOLEAN,
    /** date, time or timestamp already formatted with the user patterns */
    TEMPORAL,
    /** any other value as text */
    TEXT
}
