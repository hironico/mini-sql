package net.hironico.common.swing.ribbon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.*;

@SuppressWarnings("restriction")
public class Ribbon extends JTabbedPane {

    private static final long serialVersionUID = 1L;

    private AbstractRibbonAction exitAction = null;

    public static class RibbonAction extends AbstractRibbonAction {

        private static final long serialVersionUID = 1L;

        public RibbonAction(String title) {
            super(title);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }

    }

    public Ribbon() {
        super();
        setMaximumSize(new Dimension(4096, 148));
        setPreferredSize(new Dimension(1024, 148));
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

    public void addRibbonTab(RibbonTab pnl) {
        super.addTab(pnl.getTitle(), pnl);
    }

    public void setSelectedRibbonTab(String title) {
        for  (int index = 0; index < super.getTabCount(); index++) {
            if (super.getTitleAt(index).equals(title)) {
                super.setSelectedIndex(index);
                return;
            }
        }
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