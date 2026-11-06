package net.hironico.minisql.model.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Generates the DDL statements that re-create the tables of a {@link SchemaModel} into a target database.
 * <p>
 * Statements are produced in this order so that any foreign key can be created whatever the table order:
 * drop tables (optional), create tables with their primary key, create indexes, add foreign keys.
 * Views are skipped: the model holds their columns but not their query.
 */
public class SchemaDdlGenerator {

    /** Kind of statement, used for the execution summary and to tell which errors can be ignored */
    public enum Phase { DROP, TABLE, INDEX, FOREIGN_KEY }

    /**
     * A generated statement.
     *
     * @param phase     kind of statement
     * @param tableName source table name
     * @param sql       the SQL statement, without terminator
     */
    public record Statement(Phase phase, String tableName, String sql) {
    }

    /** Import options */
    public static class Options {
        /** Dialect of the target database */
        public SqlDialect dialect = SqlDialect.GENERIC;
        /** Target schema, null or empty to create the tables in the default schema of the connection */
        public String targetSchema;
        /** Quote table, column and constraint names */
        public boolean quoteIdentifiers = false;
        /** Generate DROP TABLE statements before the CREATE TABLE */
        public boolean dropTables = false;
        /** Re-create the column default values. Off by default: defaults are often vendor specific */
        public boolean defaultValues = false;
        /** Re-create the primary keys */
        public boolean primaryKeys = true;
        /** Re-create the indexes */
        public boolean indexes = true;
        /** Re-create the foreign keys */
        public boolean foreignKeys = true;
        /** Keep the constraint and index names of the model. If false, the target database generates the names */
        public boolean keepConstraintNames = true;
    }

    private final SchemaModel model;
    private final Collection<String> tableNames;
    private final Options options;
    private final SqlDialect sourceDialect;

    /**
     * Creates a generator.
     *
     * @param model      the exported model
     * @param tableNames names of the tables to create, in any order. Views and unknown names are ignored
     * @param options    the import options
     */
    public SchemaDdlGenerator(SchemaModel model, Collection<String> tableNames, Options options) {
        this.model = model;
        this.tableNames = tableNames;
        this.options = options;
        this.sourceDialect = SqlDialect.detect(model.source() == null ? null : model.source().product());
    }

    /** @return the dialect of the database the model was exported from */
    public SqlDialect getSourceDialect() {
        return sourceDialect;
    }

    /** @return true if the model object can be created as a table (views cannot be re-created from the model) */
    public static boolean isTable(SchemaModel.Table table) {
        return table != null && table.type() != null && !table.type().toUpperCase(Locale.ROOT).contains("VIEW");
    }

    /**
     * Get the selected tables sorted so that referenced tables come before the referencing ones.
     * Tables involved in a reference cycle are added at the end in name order.
     *
     * @return the ordered table names
     */
    public List<String> getCreationOrder() {
        Map<String, SchemaModel.Table> selected = new TreeMap<>();
        for (String name : tableNames) {
            SchemaModel.Table table = model.tables().get(name);
            if (isTable(table)) {
                selected.put(name, table);
            }
        }

        Set<String> ordered = new LinkedHashSet<>();
        boolean progress = true;
        while (progress && ordered.size() < selected.size()) {
            progress = false;
            for (Map.Entry<String, SchemaModel.Table> e : selected.entrySet()) {
                if (ordered.contains(e.getKey())) {
                    continue;
                }
                boolean ready = e.getValue().foreignKeys() == null || e.getValue().foreignKeys().stream()
                        .map(SchemaModel.ForeignKey::referencedTable)
                        .filter(selected::containsKey)
                        .filter(ref -> !ref.equals(e.getKey()))
                        .allMatch(ordered::contains);
                if (ready) {
                    ordered.add(e.getKey());
                    progress = true;
                }
            }
        }
        // reference cycles
        ordered.addAll(selected.keySet());
        return new ArrayList<>(ordered);
    }

    /**
     * Generates all the statements for the selected tables.
     *
     * @return the statements in execution order
     */
    public List<Statement> generate() {
        List<String> order = getCreationOrder();
        List<Statement> statements = new ArrayList<>();

        if (options.dropTables) {
            // drop the referencing tables first
            for (int i = order.size() - 1; i >= 0; i--) {
                statements.add(new Statement(Phase.DROP, order.get(i), options.dialect.dropTable(tableName(order.get(i)))));
            }
        }
        for (String name : order) {
            statements.add(new Statement(Phase.TABLE, name, buildCreateTable(name, model.tables().get(name))));
        }
        if (options.indexes) {
            for (String name : order) {
                buildIndexes(name, model.tables().get(name)).forEach(sql -> statements.add(new Statement(Phase.INDEX, name, sql)));
            }
        }
        if (options.foreignKeys) {
            Set<String> created = new HashSet<>(order);
            for (String name : order) {
                buildForeignKeys(name, model.tables().get(name), created)
                        .forEach(sql -> statements.add(new Statement(Phase.FOREIGN_KEY, name, sql)));
            }
        }
        return statements;
    }

    /**
     * Get the foreign keys of the selected tables that reference a table which is not part of the selection.
     * These foreign keys are not created.
     *
     * @return descriptions like "ORDERS.FK_ORDERS_CUSTOMER -> CUSTOMER"
     */
    public List<String> getSkippedForeignKeys() {
        Set<String> created = new HashSet<>(getCreationOrder());
        List<String> skipped = new ArrayList<>();
        for (String name : created.stream().sorted().toList()) {
            List<SchemaModel.ForeignKey> fks = model.tables().get(name).foreignKeys();
            if (fks == null) {
                continue;
            }
            fks.stream().filter(fk -> !created.contains(fk.referencedTable()))
                    .forEach(fk -> skipped.add(String.format("%s.%s -> %s", name, fk.name(), fk.referencedTable())));
        }
        return skipped;
    }

    /**
     * Joins the statements into a script, using the given statement separator
     * (";" or the batch separator of the connection like "GO").
     *
     * @param statements the statements
     * @param separator  the statement separator
     * @return the script text
     */
    public static String toScript(List<Statement> statements, String separator) {
        boolean ownLine = separator != null && separator.chars().allMatch(Character::isLetter);
        String sep = separator == null || separator.isBlank() ? ";" : separator.trim();
        StringBuilder script = new StringBuilder();
        Phase previous = null;
        for (Statement st : statements) {
            if (st.phase() != previous) {
                script.append("-- ").append(switch (st.phase()) {
                    case DROP -> "Drop tables";
                    case TABLE -> "Tables";
                    case INDEX -> "Indexes";
                    case FOREIGN_KEY -> "Foreign keys";
                }).append('\n');
                previous = st.phase();
            }
            script.append(st.sql()).append(ownLine ? "\n" + sep + "\n\n" : sep + "\n\n");
        }
        return script.toString();
    }

    /**
     * Quotes the identifier if the quote option is set: brackets for SQL Server,
     * backticks for MySQL, double quotes otherwise.
     *
     * @param identifier the identifier
     * @return the quoted or unchanged identifier
     */
    public String quote(String identifier) {
        if (!options.quoteIdentifiers || identifier == null) {
            return identifier;
        }
        return switch (options.dialect) {
            case SQLSERVER -> "[" + identifier.replace("]", "]]") + "]";
            case MYSQL -> "`" + identifier.replace("`", "``") + "`";
            default -> "\"" + identifier.replace("\"", "\"\"") + "\"";
        };
    }

    /** @return the target table name, prefixed with the target schema if any */
    public String tableName(String name) {
        return options.targetSchema == null || options.targetSchema.isBlank()
                ? quote(name)
                : quote(options.targetSchema.trim()) + "." + quote(name);
    }

    private String constraintName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        int max = options.dialect.maxIdentifierLength();
        return quote(name.length() > max ? name.substring(0, max) : name);
    }

    private String columnList(List<String> columns) {
        return columns.stream().map(this::quote).collect(Collectors.joining(", "));
    }

    String buildCreateTable(String name, SchemaModel.Table table) {
        SqlDialect dialect = options.dialect;
        List<String> lines = new ArrayList<>();
        for (SchemaModel.Column col : table.columns()) {
            String nativeType = SqlDialect.nativeTypeName(col);
            StringBuilder line = new StringBuilder("    ").append(quote(col.name())).append(' ')
                    .append(dialect.columnType(col, sourceDialect));

            boolean serial = sourceDialect == dialect && SqlDialect.isSerialType(nativeType);
            boolean identity = col.autoIncrement() && !serial;
            if (identity) {
                line.append(' ').append(dialect.identityClause());
            } else if (options.defaultValues && col.defaultValue() != null && !serial
                    && !isSequenceDefault(col.defaultValue())) {
                line.append(" DEFAULT ").append(col.defaultValue());
            }

            if (!col.nullable() || identity) {
                line.append(" NOT NULL");
            } else if (dialect.explicitNull()) {
                line.append(" NULL");
            }
            lines.add(line.toString());
        }

        SchemaModel.PrimaryKey pk = table.primaryKey();
        if (options.primaryKeys && pk != null && pk.columns() != null && !pk.columns().isEmpty()) {
            String pkName = options.keepConstraintNames ? constraintName(pk.name()) : null;
            lines.add("    " + (pkName == null ? "" : "CONSTRAINT " + pkName + " ")
                    + "PRIMARY KEY (" + columnList(pk.columns()) + ")");
        }

        return "CREATE TABLE " + tableName(name) + " (\n" + String.join(",\n", lines) + "\n)";
    }

    /** Sequence based defaults (PostgreSQL nextval, Oracle seq.nextval) reference objects that are not exported */
    private static boolean isSequenceDefault(String defaultValue) {
        String d = defaultValue.toLowerCase(Locale.ROOT);
        return d.contains("nextval") || d.contains("next value for");
    }

    List<String> buildIndexes(String name, SchemaModel.Table table) {
        List<String> result = new ArrayList<>();
        if (table.indexes() == null) {
            return result;
        }
        // foreign keys that will be created: their index is created by the database itself on some targets
        Set<List<String>> autoIndexedFks = new HashSet<>();
        if (options.foreignKeys && options.dialect.autoIndexesForeignKeys() && table.foreignKeys() != null) {
            Set<String> created = new HashSet<>(getCreationOrder());
            table.foreignKeys().stream().filter(fk -> created.contains(fk.referencedTable()))
                    .forEach(fk -> autoIndexedFks.add(fk.columns()));
        }

        int generated = 1;
        Set<String> signatures = new HashSet<>();
        for (SchemaModel.Index idx : table.indexes()) {
            // the primary key index is created with the constraint
            if (idx.primaryKey() || idx.columns() == null || idx.columns().isEmpty()
                    || idx.columns().stream().anyMatch(c -> c.name() == null)) {
                continue;
            }
            List<String> idxColumnNames = idx.columns().stream().map(SchemaModel.IndexColumn::name).toList();
            if (!idx.unique() && autoIndexedFks.contains(idxColumnNames)) {
                continue;
            }
            String columns = idx.columns().stream()
                    .map(c -> quote(c.name()) + ("DESC".equals(c.order()) ? " DESC" : ""))
                    .collect(Collectors.joining(", "));
            // the same columns can be reported twice (unique constraint + its index)
            if (!signatures.add(idx.unique() + ":" + columns)) {
                continue;
            }
            String indexName = options.keepConstraintNames && idx.name() != null
                    ? idx.name()
                    : name + "_IDX" + generated++;
            String qualifiedName = options.dialect.qualifiedIndexNames() && options.targetSchema != null
                    && !options.targetSchema.isBlank()
                    ? quote(options.targetSchema.trim()) + "." + constraintName(indexName)
                    : constraintName(indexName);
            result.add("CREATE " + (idx.unique() ? "UNIQUE " : "") + "INDEX " + qualifiedName
                    + " ON " + tableName(name) + " (" + columns + ")");
        }
        return result;
    }

    List<String> buildForeignKeys(String name, SchemaModel.Table table, Set<String> createdTables) {
        List<String> result = new ArrayList<>();
        if (table.foreignKeys() == null) {
            return result;
        }
        SqlDialect dialect = options.dialect;
        for (SchemaModel.ForeignKey fk : table.foreignKeys()) {
            if (!createdTables.contains(fk.referencedTable())) {
                continue;
            }
            StringBuilder sql = new StringBuilder("ALTER TABLE ").append(tableName(name)).append(" ADD ");
            String fkName = options.keepConstraintNames ? constraintName(fk.name()) : null;
            if (fkName != null) {
                sql.append("CONSTRAINT ").append(fkName).append(' ');
            }
            sql.append("FOREIGN KEY (").append(columnList(fk.columns())).append(") REFERENCES ")
                    .append(tableName(fk.referencedTable())).append(" (")
                    .append(columnList(fk.referencedColumns())).append(')');

            if (isAction(fk.onDelete()) && dialect.supportsDeleteRule(fk.onDelete())) {
                sql.append(" ON DELETE ").append(fk.onDelete());
            }
            if (isAction(fk.onUpdate()) && dialect.supportsUpdateRule(fk.onUpdate())) {
                sql.append(" ON UPDATE ").append(fk.onUpdate());
            }
            result.add(sql.toString());
        }
        return result;
    }

    /** NO ACTION and RESTRICT are the default behaviour, nothing to write */
    private static boolean isAction(String rule) {
        return "CASCADE".equals(rule) || "SET NULL".equals(rule) || "SET DEFAULT".equals(rule);
    }

    /**
     * Builds a short summary of the statements.
     *
     * @param statements the statements
     * @return a summary like "3 table(s), 2 index(es), 1 foreign key(s)"
     */
    public static String summary(List<Statement> statements) {
        Map<Phase, Long> counts = new LinkedHashMap<>();
        for (Phase p : Phase.values()) {
            counts.put(p, statements.stream().filter(s -> s.phase() == p).count());
        }
        List<String> parts = new ArrayList<>();
        if (counts.get(Phase.DROP) > 0) {
            parts.add(counts.get(Phase.DROP) + " drop(s)");
        }
        parts.add(counts.get(Phase.TABLE) + " table(s)");
        parts.add(counts.get(Phase.INDEX) + " index(es)");
        parts.add(counts.get(Phase.FOREIGN_KEY) + " foreign key(s)");
        return String.join(", ", parts);
    }
}
