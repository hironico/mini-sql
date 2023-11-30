package net.hironico.minisql.model;

import java.util.List;

public class SQLTableForeignKey extends SQLObject {
    public String fkSchemaName;
    public String fkTableName;
    public String fkColumnName;

    public String pkSchemaName;
    public String pkTableName;
    public String pkColumnName;

    public String deleteRule;
    public String updateRule;

    public int kewSeq;

    public SQLTableForeignKey(String name) {
        this(name, null, null, null, null, null, null);
    }
    public SQLTableForeignKey(String name,
                              String pkSchemaName, String pkTableName, String pkColumnName,
                              String fkSchemaName, String fkTableName, String fkColumnName) {
        super();
        this.name = name;
        this.schemaName = null;

        this.fkSchemaName = fkSchemaName;
        this.fkTableName = fkTableName;
        this.fkColumnName = fkColumnName;

        this.pkSchemaName = pkSchemaName;
        this.pkTableName = pkTableName;
        this.pkColumnName = pkColumnName;
    }


}
