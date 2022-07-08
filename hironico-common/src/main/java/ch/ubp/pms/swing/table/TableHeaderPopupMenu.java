package ch.ubp.pms.swing.table;

import java.awt.Component;
import javax.swing.JPopupMenu;

public class TableHeaderPopupMenu extends JPopupMenu {
    
    private static final long serialVersionUID = 1L;
    
	protected int clickedColumn = -1;

    public void show(Component comp, int x, int y, int col) {
        this.clickedColumn = col;
        super.show(comp, x, y);
    }

    public int getClickedColumn() {
        return this.clickedColumn;
    }
}