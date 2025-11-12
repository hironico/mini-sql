package net.hironico.common.swing.ribbon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Launches a sample ribbon app to test behaviour of the Ribbon component
 */
public class SampleRibbonApp {

    /**
     * Sample Ribbon action to demonstrate usage of actions
     */
    private static class SampleRibbonAction extends AbstractRibbonAction {

        /**
         * Builds a ribbon action
         * @param title the title text to display in Ribbon
         */
        public SampleRibbonAction(String title) {
            super(title);
        }

        /**
         * Sample action event. Does nothing by default
         * @param e the event to be processed
         */
        @Override
        public void actionPerformed(ActionEvent e) {

        }

    }

    /**
     * starts the app
     * @param args app arguments not used at this time
     */
    public static void main(String... args) {

        JFrame frame = new JFrame();
        frame.setTitle("Ribbon test app");
        frame.setSize(1024, 768);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        Ribbon ribbon = new Ribbon();

        RibbonGroup ribbonGroup1 = new RibbonGroup("Ribbon group 1");
        SampleRibbonAction action = new SampleRibbonAction("action text");
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
