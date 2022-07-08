package ch.ubp.pms.minisql.model;

/**
 * Model a SQL column metata data. Useable in Table or Views, or stored procs.
 */
public class SQLColumn {
    public String name;
    public String typeName;
    public Integer size;
    public Integer scale;
    public Boolean nullable;
    public Boolean autoIncrement;
    public String defaultValue;

    public String toString() {
        return name + " " +
               typeName + "(" + size + "," + scale + ") " + 
               (nullable ? " NULL " : " NOT NULL ") + 
               (autoIncrement ? " AUTOINCREMENT " : "") + 
               (defaultValue.equals("") ? "" : (" DEFAULT " + defaultValue));
    }
}