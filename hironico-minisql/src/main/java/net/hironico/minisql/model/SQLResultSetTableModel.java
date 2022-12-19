package net.hironico.minisql.model;

import java.time.OffsetDateTime;

import javax.swing.table.DefaultTableModel;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQL ResultSet TableModel to be displayed in the result component once the query is finished.
 * Multiple display types are possible in order to present the resultset as a table, json document, row text ...
 * It is also possible to transpose a result set and vice versa.
 */
public class SQLResultSetTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(SQLResultSetTableModel.class.getName());

    private static final long ONE_MB = 1024 * 1024;

    protected Class<?>[] classNames = new Class<?>[0];

    private String title;
    private String query;
    private boolean isTransposed = false;

    /**
     * Describe the way to display the data in the result component
     */
    private int displayType;

    public static final int DISPLAY_TYPE_TEXT = 1;
    public static final int DISPLAY_TYPE_TABLE = 2;
    public static final int DISPLAY_TYPE_JSON = 3;
    public static final int DISPLAY_TYPE_SQL = 4;

    private SQLResultSetTableModel() {
        super();
    }

    public SQLResultSetTableModel(ResultSet resultSet, String title, String query, int displayType) throws SQLException {
        super();
        setupColumns(resultSet);
        setupData(resultSet);
        this.title = title == null ? "Results" : title;
        this.query = query == null ? "N/A" : query;
        this.displayType = displayType;
    }

    private void setupData(ResultSet resultSet) throws SQLException {

        if (resultSet == null) {
            Object[] row = new Object[this.classNames.length];
            row[0] = "This query did not return any result set.";
            this.addRow(row);
            return;
        }

        int columnCount = resultSet.getMetaData().getColumnCount();

        while (resultSet.next()) {

            byte[] buffer = new byte[(int)ONE_MB];

            Object[] row = new Object[columnCount];             
            for (int index = 1; index <= columnCount; index++) {
                Object obj = resultSet.getObject(index);

                // if clob then read at most one megabytes of data
                if (obj instanceof java.sql.Clob) {
                    try {
                        java.sql.Clob clob = (java.sql.Clob)obj;
                        InputStream in = clob.getAsciiStream();
                        int readCount = in.read(buffer);
                        in.close();
                        row[index - 1] = new String(buffer, 0, readCount);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Could not read Clob content.", ex);
                        row[index-1] = "Could not retreive Clob content. " + ex.getMessage();                        
                    }
                } else if (!resultSet.wasNull()) {
                    // oracle timestamp support
                    String clazz = obj.getClass().getName();
                    switch(clazz) {
                        case "oracle.common.TIMESTAMP":
                        case "oracle.common.TIMESTAMPTZ":
                        case "oracle.common.TIMESTAMPLTZ":
                            row[index - 1] = resultSet.getObject(index, OffsetDateTime.class);
                            break;

                        default:
                            row[index - 1] = obj;
                            break;
                    }
                } else {
                    // null !
                    row[index - 1] = null;
                }
            }
            this.addRow(row);
        }
    }

    private void setupColumns(ResultSet resultSet) throws SQLException {
        String[] columnNames = new String[1];
        Class<?>[] columnClasses = new Class<?>[1];
        if (resultSet == null) {
            columnClasses[0] = String.class;
            columnNames[0] = "Message(s)";
        } else {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            columnNames = new String[columnCount];
            columnClasses = new Class<?>[columnCount];
            for (int index = 1; index <= columnCount; index++) {
                columnNames[index - 1] = metaData.getColumnLabel(index);
                try {
                    columnClasses[index - 1] = Class.forName(metaData.getColumnClassName(index));
                } catch (Exception ex) {
                    columnClasses[index - 1] = Object.class;
                }
            }
        }
        setColumnIdentifiers(columnNames);
        setColumnClasses(columnClasses);
    }

    public SQLResultSetTableModel transpose() throws Exception {

        if (this.isTransposed) {
            LOGGER.severe("Cannot transpose an already transposed sql table model.");
            throw new Exception("Cannot transpose an already transposed sql table model.");
        }

        SQLResultSetTableModel newModel = new SQLResultSetTableModel();
        newModel.displayType = this.displayType;
        newModel.title = this.title;
        newModel.query = this.query;
        newModel.isTransposed = true;

        Class<?>[] newColClasses = new Class<?>[this.getRowCount() + 1];
        String[] newColNames = new String[this.getRowCount() + 1];
        newColClasses[0] = String.class;
        newColNames[0] = "Property";
        for(int i = 0; i < this.getRowCount(); i++) {
            newColClasses[i+1] = Object.class;
            newColNames[i+1] = String.format("Value %d", i);
        }
        newModel.setColumnClasses(newColClasses);
        newModel.setColumnIdentifiers(newColNames);

        for(int col = 0; col < this.getColumnCount(); col++) {
            Object[] newRow = new Object[this.getRowCount() + 1];
            newRow[0] = this.getColumnName(col);
            for (int row = 0; row < this.getRowCount(); row++) {
                newRow[row + 1] = this.getValueAt(row, col);
            }
            newModel.addRow(newRow);
        }

        return newModel;
    }

    private void setColumnClasses(Class<?>[] columnClasses) {
        this.classNames = columnClasses;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return classNames[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public String getTitle() {
        return this.title;
    }

    public String getQuery() {
        return this.query;
    }

    public void setDisplayType(int displayType) {
        this.displayType = displayType;
    }

    public int getDisplayType() {
        return this.displayType;
    }
}