package net.hironico.common.swing.ribbon;

import com.formdev.flatlaf.icons.FlatWindowCloseIcon;
import com.formdev.flatlaf.icons.FlatWindowIconifyIcon;
import com.formdev.flatlaf.icons.FlatWindowMaximizeIcon;
import com.formdev.flatlaf.icons.FlatWindowRestoreIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public class RibbonWindowUI extends LayerUI<Ribbon> {

    protected final Color hoverBackground = UIManager.getColor("TitlePane.closeHoverBackground");
    // protected final Color pressedForeground = UIManager.getColor("TitlePane.closePressedForeground");

    private RibbonCloseIcon iconClose = null;
    private Icon iconMaximize = null;
    private Icon iconRestore = null;
    private Icon iconMinimize = null;

    static class RibbonCloseIcon extends FlatWindowCloseIcon {
        private boolean selected = false;
        protected final Color hoverForeground = UIManager.getColor("TitlePane.closeHoverForeground");

        @Override
        protected Color getForeground(Component c) {
            return selected ? this.hoverForeground : super.getForeground(c);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return this.selected;
        }
    }

    public RibbonWindowUI() {
        initialize();
    }

    private void initialize() {
        this.iconClose = new RibbonCloseIcon();
        this.iconMaximize = new FlatWindowMaximizeIcon();
        this.iconRestore = new FlatWindowRestoreIcon();
        this.iconMinimize = new FlatWindowIconifyIcon();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        int w = c.getWidth();
        int h = c.getHeight();

        if (w == 0 || h == 0) {
            return;
        }

        if (!c.isVisible() || !c.isValid()) {
            return;
        }

        // Paint the view.
        super.paint(g, c);

        int iconWidth = iconClose.getIconWidth();
        int iconHeight = iconClose.getIconHeight();

        if (iconClose.isSelected()) {
            g.setColor(this.hoverBackground);
            g.fillRect(w - iconWidth, 0, iconWidth, iconHeight);
        }
        iconClose.paintIcon(c, g, w - iconWidth, 0);

        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(c);
        if (topFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            iconRestore.paintIcon(c, g, w - iconWidth * 2, 0);
        } else {
            iconMaximize.paintIcon(c, g, w - iconWidth * 2, 0);
        }

        iconMinimize.paintIcon(c, g, w - iconWidth * 3, 0);
    }

    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends Ribbon> l) {
        if (e.getClickCount() != 1 || e.getID() != MouseEvent.MOUSE_CLICKED) {
            return;
        }

        Ribbon ribbon = l.getView();
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(ribbon);

        if (isOverIcon(this.iconClose, e, l)) {
            if (ribbon.getExitAction() != null) {
                ribbon.getExitAction().actionPerformed(null);
            } else {
                topFrame.dispatchEvent(new WindowEvent(topFrame, WindowEvent.WINDOW_CLOSING));
            }
        }

        if (isOverIcon(this.iconMaximize, e, l)) {
            if (topFrame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                topFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                topFrame.setExtendedState(JFrame.NORMAL);
            }
        }

        if (isOverIcon(this.iconMinimize, e, l)) {
            topFrame.setExtendedState(JFrame.ICONIFIED);
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends Ribbon> l) {
        if (isOverIcon(this.iconClose, e, l)) {
            this.iconClose.setSelected(true);
            l.repaint();
        } else {
            this.iconClose.setSelected(false);
            l.repaint();
        }
    }

    private boolean isOverIcon(Icon icon, MouseEvent evt, JLayer<? extends Ribbon> layer) {
        int w = layer.getWidth();
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();

        int x = w - iconWidth - 5;
        if (icon.equals(this.iconMaximize)) {
            x = w - iconWidth * 2 - 5;
        }
        if (icon.equals(this.iconMinimize)) {
            x = w - iconWidth * 3 - 5;
        }

        Rectangle rect = new Rectangle(x, 2, iconWidth, iconHeight);
        return rect.contains(evt.getPoint());
    }
}
