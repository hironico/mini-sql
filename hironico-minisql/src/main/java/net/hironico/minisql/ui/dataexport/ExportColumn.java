package net.hironico.minisql.ui.dataexport;

/**
 * A column of the table or view to export, with its selection state.
 */
public class ExportColumn {

    /** Drivers report this size, or more, for unbounded columns */
    public static final int UNBOUNDED_SIZE = Integer.MAX_VALUE - 1000;

    private final String name;
    private final String typeName;
    private final int sqlType;
    private final int size;
    private final int scale;
    private final boolean nullable;
    private boolean selected = true;

    /**
     * @param name     column name
     * @param typeName database specific type name, for display
     * @param sqlType  {@link java.sql.Types} constant
     */
    public ExportColumn(String name, String typeName, int sqlType) {
        this(name, typeName, sqlType, 0, 0, true);
    }

    /**
     * @param name     column name
     * @param typeName database specific type name, for display
     * @param sqlType  {@link java.sql.Types} constant
     * @param size     length or precision of the column, 0 or negative if unknown
     * @param scale    number of decimals for numeric columns, 0 if none or unknown
     * @param nullable true if the column accepts NULL values or if the driver does not know
     */
    public ExportColumn(String name, String typeName, int sqlType, int size, int scale, boolean nullable) {
        this.name = name;
        this.typeName = typeName;
        this.sqlType = sqlType;
        this.size = size;
        this.scale = scale;
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getSqlType() {
        return sqlType;
    }

    /** @return the length or precision of the column, 0 or negative if unknown */
    public int getSize() {
        return size;
    }

    public int getScale() {
        return scale;
    }

    /** @return true if the column accepts NULL values (or if this is unknown) */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * @return the size for display: the length, "precision,scale" for decimals, "max" for unbounded
     * columns such as text or LOBs and an empty string if unknown
     */
    public String getSizeLabel() {
        if (size <= 0) {
            return "";
        }
        if (size >= UNBOUNDED_SIZE) {
            return "max";
        }
        if (scale > 0) {
            return size + "," + scale;
        }
        return String.valueOf(size);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
