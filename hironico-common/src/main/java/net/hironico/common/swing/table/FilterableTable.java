package net.hironico.common.swing.table;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.sort.TableSortController;

public class FilterableTable extends JXTable {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, String> filters = new HashMap<>();

    private TableHeaderPopupMenu headerPopupMenu = null;
    private JMenuItem itemFilter = null;
    private JMenuItem itemClearAllFilters = null;
    private JMenuItem itemClearFilter = null;
    private JMenuItem itemPackAllColums = null;
    private JMenuItem itemPackColumn = null;

    private final TableColumnAdjuster tableColumnAdjuster = new TableColumnAdjuster(this);

    public FilterableTable() {
        super();
        initialize();
    }

    protected void applyAllFilters() {
        //If current expression doesn't parse, don't update.
        try {
            List<RowFilter<TableModel, Object>> filterList = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : filters.entrySet()) {
                Integer col = entry.getKey();
                String regExp = entry.getValue();
                RowFilter<TableModel, Object> rf = RowFilter.regexFilter(regExp, col);
                filterList.add(rf);
            }

            TableSortController<? extends TableModel> rowSorter = (TableSortController<? extends TableModel>) (FilterableTable.this
                    .getRowSorter());
            RowFilter<TableModel, Object> andFilter = RowFilter.andFilter(filterList);
            rowSorter.setRowFilter(andFilter);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }

        adaptFilteredColumnNames();
    }

    public void clearAllFilters() {
        filters.clear();

        applyAllFilters();
    }

    public void applyFilter(String regExp, int column) {
        filters.put(column, regExp);

        applyAllFilters();
    }

    public void clearFilter(int column) {
        filters.remove(column);

        applyAllFilters();
    }

    public boolean isFiltered(int col) {
        String filter = filters.get(col);
        return filter != null && !"".equals(filter);
    }

    protected String getFilteredColumnName(int col) {
        DefaultTableModel model = (DefaultTableModel) getModel();
        String title = model.getColumnName(col);
        if (isFiltered(col)) {
            if (!title.endsWith("(*)")) {
                title = title + "(*)";
            }
        } else {
            title = title.replaceAll("\\(\\*\\)", "");
        }
        return title;
    }

    protected void adaptFilteredColumnNames() {
        DefaultTableModel model = (DefaultTableModel) getModel();
        String[] names = new String[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            names[i] = getFilteredColumnName(i);
        }
        model.setColumnIdentifiers(names);
    }

    protected void initialize() {
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
        this.setAutoCreateRowSorter(true);
        this.setColumnControlVisible(true);

        MouseAdapter adapter = new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (evt.getButton() >= MouseEvent.BUTTON2) {
                    TableHeaderPopupMenu menu = getHeaderPopupMenu();
                    int col = FilterableTable.this.columnAtPoint(evt.getPoint());
                    menu.show(evt.getComponent(), evt.getX(), evt.getY(), col);
                }
            }
        };

        new ResizeColumnListener(this, tableColumnAdjuster);

        // DefaultTableHeaderCellRenderer renderer = new DefaultTableHeaderCellRenderer();
        TableCellRenderer renderer = getTableHeader().getDefaultRenderer();
        FilterableTableHeaderRenderer headerRenderer = new FilterableTableHeaderRenderer(renderer);
        getTableHeader().setDefaultRenderer(headerRenderer);

        getTableHeader().addMouseListener(adapter);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_P && e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) {
                    FilterableTable.this.tableColumnAdjuster.adjustColumns();
                }
            }
        });
    }

    protected TableHeaderPopupMenu getHeaderPopupMenu() {
        if (headerPopupMenu == null) {
            headerPopupMenu = new TableHeaderPopupMenu();
            headerPopupMenu.add(getItemFilter());
            headerPopupMenu.add(getItemClearFilter());
            headerPopupMenu.addSeparator();
            headerPopupMenu.add(getItemClearAllFilters());
            headerPopupMenu.addSeparator();
            headerPopupMenu.add(getItemPackAllColums());
            headerPopupMenu.add(getItemPackColumn());
        }

        return headerPopupMenu;
    }

    protected JMenuItem getItemPackColumn() {
        if (itemPackColumn == null) {
            itemPackColumn = new JMenuItem();
            itemPackColumn.setText("Pack column");
            itemPackColumn.addActionListener((evt) -> {
                int col = getHeaderPopupMenu().getClickedColumn();
                FilterableTable.this.tableColumnAdjuster.adjustColumn(col);
            });
        }

        return itemPackColumn;
    }

    protected JMenuItem getItemPackAllColums() {
        if (itemPackAllColums == null) {
            itemPackAllColums = new JMenuItem();
            itemPackAllColums.setText("Pack all columns");

            itemPackAllColums.addActionListener((evt) -> FilterableTable.this.tableColumnAdjuster.adjustColumns());
        }
        return itemPackAllColums;
    }

    protected JMenuItem getItemClearAllFilters() {
        if (itemClearAllFilters == null) {
            itemClearAllFilters = new JMenuItem();
            itemClearAllFilters.setText("Clear all filters");

            itemClearAllFilters.addActionListener((evt) -> FilterableTable.this.clearAllFilters());
        }

        return itemClearAllFilters;
    }

    protected JMenuItem getItemClearFilter() {
        if (itemClearFilter == null) {
            itemClearFilter = new JMenuItem();
            itemClearFilter.setText("Clear filter");

            itemClearFilter.addActionListener((evt) -> {
                int col = getHeaderPopupMenu().getClickedColumn();
                FilterableTable.this.clearFilter(col);
            });
        }

        return itemClearFilter;
    }

    protected JMenuItem getItemFilter() {
        if (itemFilter == null) {
            itemFilter = new JMenuItem();
            itemFilter.setText("Filter...");

            itemFilter.addActionListener(evt -> {
                int col = getHeaderPopupMenu().getClickedColumn();
                String columnFilter = filters.get(col);
                String newFilter = JOptionPane.showInputDialog(FilterableTable.this.getParent(),
                        "Enter column filter:\n(Empty to clear filter)", columnFilter);
                if (newFilter == null) {
                    return;
                }
                FilterableTable.this.applyFilter(newFilter, col);
                FilterableTable.this.adaptFilteredColumnNames();
                FilterableTable.this.tableColumnAdjuster.adjustColumn(col);
            });
        }

        return itemFilter;
    }
}