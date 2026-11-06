package net.hironico.minisql.ui.dataimport;

/**
 * Describes how one column of a CSV file is mapped to a column of the target database table.
 */
public class ImportColumn {

    /** Default SQL data type proposed for every imported column */
    public static final String DEFAULT_SQL_TYPE = "VARCHAR(150)";

    /** Index of the column in the CSV records (0 based) */
    private final int sourceIndex;

    /** Name of the column as read in the CSV header */
    private final String sourceName;

    /** True if this column must be created and imported */
    private boolean included = true;

    /** Name of the column in the target table */
    private String name;

    /** SQL data type used in the CREATE TABLE statement */
    private String sqlType = DEFAULT_SQL_TYPE;

    /** True if the column accepts NULL values */
    private boolean nullable = true;

    /**
     * Creates a new import column.
     *
     * @param sourceIndex index of the column in the CSV records
     * @param sourceName  name of the column in the CSV header
     * @param name        name of the column in the target table
     */
    public ImportColumn(int sourceIndex, String sourceName, String name) {
        this.sourceIndex = sourceIndex;
        this.sourceName = sourceName;
        this.name = name;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public String getSourceName() {
        return sourceName;
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
}
