package net.hironico.minisql.model;

public enum SQLObjectTypeEnum {
    TABLE("TABLE"),
    SYSTEM_TABLE("SYSTEM TABLE"),
    VIEW("VIEW"),
    SYSTEM_VIEW("SYSTEM VIEW"),
    MATERIALIZED_VIEW("MATERIALIZED VIEW"),
    PROCEDURE("PROCEDURE"),
    SYSTEM_PROCEDURE("SYSTEM PROCEDURE"),
    FUNCTION("FUNCTION"),
    SYSTEM_FUNCTION("SYSTEM FUNCTION"),
    SEQUENCE("SEQUENCE"),
    SYSTEM_SEQUENCE("SYSTEM SEQUENCE"),
    INDEX("INDEX"),
    SYSTEM_INDEX("SYSTEM INDEX"),
    SCHEMA("SCHEMA"),
    DATABASE("DATABASE"),
    UNKNOWN("UNKNOWN");

    private String value;

    SQLObjectTypeEnum(String str) {
        this.value = str;
    }

    public static boolean isTableOrView(SQLObjectTypeEnum typeEnum) {
        return typeEnum == TABLE || typeEnum == VIEW || typeEnum == MATERIALIZED_VIEW
                || typeEnum == SYSTEM_TABLE || typeEnum == SYSTEM_VIEW;
    }

    public static boolean isProcOrFunction(SQLObjectTypeEnum typeEnum) {
        return typeEnum == FUNCTION || typeEnum == PROCEDURE
                || typeEnum == SYSTEM_FUNCTION || typeEnum == SYSTEM_PROCEDURE;
    }

    public String toString() {
        return this.value;
    }

    public static SQLObjectTypeEnum valueOfStr(String str) {
        for(SQLObjectTypeEnum t : SQLObjectTypeEnum.values()) {
            if (t.value.equalsIgnoreCase(str)) {
                return t;
            }
        }

        throw new IllegalArgumentException(String.format("%s is not an SQLObjectTypeEnum value.", str));
    }
}
