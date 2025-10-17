package net.hironico.common.swing.ribbon;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("restriction")
public class Ribbon extends JTabbedPane {

    private AbstractRibbonAction exitAction = null;

    private static class RibbonAction extends AbstractRibbonAction {

        public RibbonAction(String title) {
            super(title);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }

    }

    public Ribbon() {
        super();
        int scaledHeight = UIScale.scale(148);
        setMaximumSize(new Dimension(4096, scaledHeight));
        setPreferredSize(new Dimension(1024, scaledHeight));
        this.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_CONTENT_SEPARATOR, false);
    }

    /**
     * Set a custom exit action to execute when the ribbon close icon is clicked.
     * Most of the time you may want to add a windows closing listener on the top JFrame of the application.
     * @param exitAction the action to execute when the ribbon close icon is clicked.
     */
    public void setExitAction(AbstractRibbonAction exitAction) {
        this.exitAction = exitAction;
    }

    public AbstractRibbonAction getExitAction() {
        return exitAction;
    }

    public void addRibbonTab(RibbonTab ribbonTab) {
        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0d;
        gbc.weighty = 1.0d;
        gbc.fill = GridBagConstraints.BOTH;
        container.add(ribbonTab, gbc);
        container.setOpaque(true);
        container.setBackground(this.getBackground());
        super.addTab(ribbonTab.getTitle(), container);
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

    public void setSelectedRibbonTab(RibbonTab tab) {
        this.setSelectedRibbonTab(tab.getTitle());
    }

    public static void main(String... args) {

        JFrame frame = new JFrame();
        frame.setTitle("Ribbon test app");
        frame.setSize(1024, 768);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        Ribbon ribbon = new Ribbon();

        RibbonGroup ribbonGroup1 = new RibbonGroup("Ribbon group 1");
        RibbonAction action = new RibbonAction("action text");
        ribbonGroup1.addButton(action, RibbonGroup.LARGE);
        ribbonGroup1.addButton(action, RibbonGroup.LARGE);
        ribbonGroup1.addButton(action, RibbonGroup.SMALL);
        ribbonGroup1.addButton(action, RibbonGroup.SMALL);
        ribbonGroup1.addButton(action, RibbonGroup.LARGE);
        ribbonGroup1.addButton(action, RibbonGroup.SMALL);
        ribbonGroup1.addButton(action, RibbonGroup.LARGE);
        ribbonGroup1.addButton(action, RibbonGroup.SMALL);
        ribbonGroup1.addButton(action, RibbonGroup.SMALL);
        ribbonGroup1.addButton(action, RibbonGroup.SMALL);

        RibbonGroup ribbonGroup2 = new RibbonGroup("Ribbon group 2");
        ribbonGroup2.addButton(action, RibbonGroup.LARGE);
        ribbonGroup2.addButton(action, RibbonGroup.SMALL);
        ribbonGroup2.addButton(action, RibbonGroup.SMALL);
        ribbonGroup2.addButton(action, RibbonGroup.SMALL);

        RibbonGroup ribbonGroup3 = new RibbonGroup("Ribbon group 3");
        ribbonGroup3.addButton(action, RibbonGroup.LARGE);
        ribbonGroup3.addButton(action, RibbonGroup.SMALL);
        ribbonGroup3.addButton(action, RibbonGroup.SMALL);
        ribbonGroup3.addButton(action, RibbonGroup.SMALL);
        ribbonGroup3.addButton(action, RibbonGroup.SMALL);

        RibbonTab ribbonTab = new RibbonTab("Tab 1");
        ribbonTab.addGroup(ribbonGroup1);
        ribbonTab.addGroup(ribbonGroup2);

        RibbonTab ribbonTab2 = new RibbonTab("Tab 2");
        ribbonTab2.addGroup(ribbonGroup3);

        ribbon.addRibbonTab(ribbonTab);
        ribbon.addRibbonTab(ribbonTab2);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(ribbon, BorderLayout.PAGE_START);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }
}