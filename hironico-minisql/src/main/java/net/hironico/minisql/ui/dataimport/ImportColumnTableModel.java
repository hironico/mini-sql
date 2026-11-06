package net.hironico.minisql.ui.dataimport;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model used by the table structure step of the import wizard.
 * Columns: Include, CSV column, Column name, SQL type, Nullable.
 */
public class ImportColumnTableModel extends AbstractTableModel {

    public static final int COL_INCLUDE = 0;
    public static final int COL_SOURCE = 1;
    public static final int COL_NAME = 2;
    public static final int COL_TYPE = 3;
    public static final int COL_NULLABLE = 4;

    private static final String[] COLUMN_NAMES = {"Include", "CSV column", "Column name", "SQL type", "Nullable"};

    private final List<ImportColumn> columns = new ArrayList<>();

    /**
     * Replaces the columns displayed by this model.
     *
     * @param newColumns the new import columns
     */
    public void setColumns(List<ImportColumn> newColumns) {
        columns.clear();
        columns.addAll(newColumns);
        fireTableDataChanged();
    }

    /** @return the live list of import columns */
    public List<ImportColumn> getColumns() {
        return columns;
    }

    /**
     * Sets the SQL type of all the given rows.
     *
     * @param rows    model row indexes
     * @param sqlType SQL type to apply
     */
    public void setSqlType(int[] rows, String sqlType) {
        for (int row : rows) {
            columns.get(row).setSqlType(sqlType);
        }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return columns.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return (columnIndex == COL_INCLUDE || columnIndex == COL_NULLABLE) ? Boolean.class : String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != COL_SOURCE;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ImportColumn col = columns.get(rowIndex);
        return switch (columnIndex) {
            case COL_INCLUDE -> col.isIncluded();
            case COL_SOURCE -> col.getSourceName();
            case COL_NAME -> col.getName();
            case COL_TYPE -> col.getSqlType();
            case COL_NULLABLE -> col.isNullable();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ImportColumn col = columns.get(rowIndex);
        switch (columnIndex) {
            case COL_INCLUDE -> col.setIncluded(Boolean.TRUE.equals(value));
            case COL_NAME -> {
                String name = value == null ? "" : value.toString().trim();
                if (!name.isEmpty()) {
                    col.setName(name);
                }
            }
            case COL_TYPE -> {
                String type = value == null ? "" : value.toString().trim();
                col.setSqlType(type.isEmpty() ? ImportColumn.DEFAULT_SQL_TYPE : type);
            }
            case COL_NULLABLE -> col.setNullable(Boolean.TRUE.equals(value));
            default -> {
                return;
            }
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }
}
