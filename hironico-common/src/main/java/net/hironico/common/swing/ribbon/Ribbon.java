package net.hironico.common.swing.ribbon;

import com.formdev.flatlaf.util.UIScale;
import net.hironico.common.swing.tabbedpane.JTabbedPaneNoContentSeparator;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

@SuppressWarnings("restriction")
public class Ribbon extends JTabbedPaneNoContentSeparator {

    private AbstractRibbonAction exitAction = null;

    public Ribbon() {
        super();
        int scaledHeight = UIScale.scale(155);
        setMaximumSize(new Dimension(4096, scaledHeight));
        setPreferredSize(new Dimension(1024, scaledHeight));
    }

    /**
     * Set a custom exit action to execute when the ribbon close icon is clicked.
     * Most of the time you may want to add a windows closing listener on the top JFrame of the application.
     * @param exitAction the action to execute when the ribbon close icon is clicked.
     */
    public void setExitAction(AbstractRibbonAction exitAction) {
        this.exitAction = exitAction;
    }

    /**
     * Get the overrien exit action to execute for this ribbon
     * @return action to execute when exiting
     */
    public AbstractRibbonAction getExitAction() {
        return exitAction;
    }

    public void addRibbonTab(RibbonTab ribbonTab) {
        addTab(ribbonTab.getTitle(), ribbonTab);
    }

    /**
     * Selects the ribbon tab whose name is given in parameter.
     * Returns the RibbonTab found and null if no ribbon tab is found.
     * If more than one ribbon tab have the same name, then the first one is returned.
     * @param title the title of the ribbon tab we want to select
     * @return RibbonTab instance found or null if none found for that title
     */
    public RibbonTab setSelectedRibbonTab(String title) {
        for  (int index = 0; index < super.getTabCount(); index++) {
            if (super.getTitleAt(index).equals(title)) {
                super.setSelectedIndex(index);
                if (super.getComponentAt(index) instanceof JPanel pnl) {
                    java.util.List<Component> ribbonTab = Arrays.stream(pnl.getComponents()).filter(c -> c instanceof RibbonTab).toList();
                    return ribbonTab.isEmpty() ? null : (RibbonTab) ribbonTab.get(0);
                }
            }
        }

        // not found

        return null;
    }
}