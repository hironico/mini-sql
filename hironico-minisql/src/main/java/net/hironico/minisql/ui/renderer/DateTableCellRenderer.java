package net.hironico.minisql.ui.renderer;

import java.awt.Component;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import oracle.sql.TIMESTAMPTZ;

public class DateTableCellRenderer implements TableCellRenderer {

    private final DateTimeFormatter formatter;
    private final TableCellRenderer delegate;

    public DateTableCellRenderer(TableCellRenderer delegate) {
        this(delegate, "yyyy-MM-dd HH:mm:ss");
    }

    public DateTableCellRenderer(TableCellRenderer delegate, String dateTimePattern) {
        super();
        this.delegate = delegate;
        this.formatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        // oracle SQL TIMESTAMPZ
        if (value instanceof TIMESTAMPTZ) {
            TIMESTAMPTZ tz = (TIMESTAMPTZ)value;
            System.out.println(tz.toString());
        }

        if (value instanceof Date) {
            Timestamp valueDate = (Timestamp)value;
            LocalDateTime dt = LocalDateTime.ofInstant(valueDate.toInstant(), ZoneId.of("Europe/Zurich"));
            return this.delegate.getTableCellRendererComponent(table, dt.format(formatter), isSelected, hasFocus, row, column);
        } else {
            return this.delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
