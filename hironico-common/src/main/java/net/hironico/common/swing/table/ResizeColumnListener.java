package net.hironico.common.swing.table;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Resize column listener is a MouseListener that catch double click on the edge of the column header
 * in order to triger an automatic resize of the table column regarding the header lable and the values 
 * in the column.
 */
public class ResizeColumnListener implements MouseListener {
    private static final Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

    private MouseListener uiListener;
    private final JTableHeader header;
    private final TableColumnAdjuster tca;

    public ResizeColumnListener(JTable table) {
        this(table, null);
    }
    public ResizeColumnListener(JTable table, TableColumnAdjuster tca) {
        this.header = table.getTableHeader();
        this.tca = tca == null ? new TableColumnAdjuster(table) : tca;

        MouseListener[] listeners = header.getMouseListeners();

        for (MouseListener ml : listeners) {
            String className = ml.getClass().toString();

            if (className.contains("BasicTableHeaderUI")) {
                uiListener = ml;
                header.removeMouseListener(ml);
                header.addMouseListener(this);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (header.getCursor() != resizeCursor) {
            uiListener.mouseClicked(e);
            return;
        }

        //  Handle the double click event to resize the column
        //  Note: The last 3 pixels + 3 pixels of next column are for resizing,
        //  so we need to adjust the mouse point to get the actual column.

        if (e.getClickCount() == 2) {
            Point p = e.getPoint();
            p.x -= 3;
            int column = header.columnAtPoint(p);
            tca.adjustColumn(column);

            //  Generate event to reset the cursor

            header.dispatchEvent(new MouseEvent(header, MouseEvent.MOUSE_MOVED, e.getWhen(), e.getModifiersEx(), e.getX(),
                    e.getY(), 0, false));
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        uiListener.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        uiListener.mouseExited(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        uiListener.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        uiListener.mouseReleased(e);
    }
}