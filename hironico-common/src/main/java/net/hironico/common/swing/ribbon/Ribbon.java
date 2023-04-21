package net.hironico.common.swing.ribbon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

@SuppressWarnings("restriction")
public class Ribbon extends JTabbedPane {

    private static final long serialVersionUID = 1L;

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
        setMaximumSize(new Dimension(4096, 172));
        setPreferredSize(new Dimension(1024, 172));
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
        ribbonGroup1.addAction(action, RibbonGroup.LARGE);
        ribbonGroup1.addAction(action, RibbonGroup.LARGE);
        ribbonGroup1.addAction(action, RibbonGroup.SMALL);
        ribbonGroup1.addAction(action, RibbonGroup.SMALL);
        ribbonGroup1.addAction(action, RibbonGroup.LARGE);
        ribbonGroup1.addAction(action, RibbonGroup.SMALL);
        ribbonGroup1.addAction(action, RibbonGroup.LARGE);
        ribbonGroup1.addAction(action, RibbonGroup.SMALL);
        ribbonGroup1.addAction(action, RibbonGroup.SMALL);
        ribbonGroup1.addAction(action, RibbonGroup.SMALL);

        RibbonGroup ribbonGroup2 = new RibbonGroup("Ribbon group 2");
        ribbonGroup2.addAction(action, RibbonGroup.LARGE);
        ribbonGroup2.addAction(action, RibbonGroup.SMALL);
        ribbonGroup2.addAction(action, RibbonGroup.SMALL);
        ribbonGroup2.addAction(action, RibbonGroup.SMALL);

        RibbonGroup ribbonGroup3 = new RibbonGroup("Ribbon group 3");
        ribbonGroup3.addAction(action, RibbonGroup.LARGE);
        ribbonGroup3.addAction(action, RibbonGroup.SMALL);
        ribbonGroup3.addAction(action, RibbonGroup.SMALL);
        ribbonGroup3.addAction(action, RibbonGroup.SMALL);
        ribbonGroup3.addAction(action, RibbonGroup.SMALL);

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