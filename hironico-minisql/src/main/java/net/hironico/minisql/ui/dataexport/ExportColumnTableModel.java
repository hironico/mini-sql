package net.hironico.minisql.ui.dataexport;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model listing the columns of the object to export with a checkbox to select them.
 */
public class ExportColumnTableModel extends AbstractTableModel {

    private static final String[] HEADERS = {"Export", "Column", "Type", "Size", "Nullable"};

    private final List<ExportColumn> columns = new ArrayList<>();

    public void setColumns(List<ExportColumn> newColumns) {
        columns.clear();
        columns.addAll(newColumns);
        fireTableDataChanged();
    }

    public void selectAll(boolean selected) {
        columns.forEach(c -> c.setSelected(selected));
        fireTableDataChanged();
    }

    /** @return the names of the selected columns, in table order */
    public List<String> getSelectedColumnNames() {
        List<String> names = new ArrayList<>();
        for (ExportColumn col : columns) {
            if (col.isSelected()) {
                names.add(col.getName());
            }
        }
        return names;
    }

    @Override
    public int getRowCount() {
        return columns.size();
    }

    @Override
    public int getColumnCount() {
        return HEADERS.length;
    }

    @Override
    public String getColumnName(int column) {
        return HEADERS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return (columnIndex == 0 || columnIndex == 4) ? Boolean.class : String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ExportColumn col = columns.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> col.isSelected();
            case 1 -> col.getName();
            case 2 -> col.getTypeName();
            case 3 -> col.getSizeLabel();
            default -> col.isNullable();
        };
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            columns.get(rowIndex).setSelected(Boolean.TRUE.equals(value));
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
