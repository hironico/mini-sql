package net.hironico.minisql.ui;

import net.hironico.common.swing.ribbon.*;
import net.hironico.minisql.App;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ui.batch.BatchPanel;
import net.hironico.minisql.ui.batch.action.NewBatchAction;
import net.hironico.minisql.ui.batch.ribbon.BatchRibbonTab;
import net.hironico.minisql.ui.config.ShowConfigPanelAction;
import net.hironico.common.swing.tabbedpane.JTabbedPaneNoContentSeparator;
import net.hironico.minisql.ui.dbexplorer.SchemaExplorerPanel;
import net.hironico.minisql.ui.dbexplorer.ribbon.DbExplorerRibbonTab;
import net.hironico.minisql.ui.editor.QueryPanel;
import net.hironico.minisql.ui.editor.ribbon.EditorRibbonTab;
import net.hironico.minisql.ui.editor.ribbon.FileRibbonGroup;
import net.hironico.minisql.ui.history.QueryHistoryPanel;
import net.hironico.common.swing.tabbedpane.CloseableTabComponent;
import net.hironico.common.swing.JSplitPaneNoDivider;
import net.hironico.minisql.ui.visualdb.VisualDbPanel;
import net.hironico.minisql.ui.visualdb.action.NewVisualDbTabAction;
import net.hironico.minisql.ui.visualdb.ribbon.VisualDbRibbonTab;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class MainWindow extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(MainWindow.class.getName());

    public static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static final Map<String, AbstractRibbonAction> appActions = new HashMap<>();

    private static final MainWindow instance = new MainWindow();

    private Ribbon ribbon = null;
    private RibbonTab homeRibbonTab = null;
    private RibbonTab editorRibbonTab = null;
    private RibbonGroup fileRibbonGroup = null;
    private RibbonGroup systemRibbonGroup = null;
    private RibbonGroup toolsRibbonGroup = null;
    private RibbonTab explorerRibbonTab = null;
    private RibbonTab visualDbRibbonTab = null;
    private RibbonTab batchRibbonTab = null;
    
    private JSplitPaneNoDivider splitMain = null;

    private JTabbedPaneNoContentSeparator tabExplorer = null;
    private SchemaExplorerPanel schemaExplorerPanel = null;

    private JTabbedPaneNoContentSeparator tabEditors = null;
    private JSplitPaneNoDivider splitEditor = null;

    private JTabbedPaneNoContentSeparator tabTools = null;
    private QueryHistoryPanel pnlHistory = null;

    private JPanel pnlStatusBar = null;
    private JLabel lblConnectionStatus = null;    

    private MainWindow() {
        super("MiniSQL");

        // init actions
        initActions();

        // init view
        initialize();
    }

    /**
     * Get the application main window instance.
     * @return MainWindow of the application.
     */
    public static MainWindow getInstance() {
        return instance;
    }

    private void initialize() {
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                MainWindow.this.setSplitSizes();
            }
        });  
        
        this.addWindowStateListener((evt) -> {
            int state = evt.getNewState();
            if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                MainWindow.this.setSplitSizes();
            } else {
                if ((state & Frame.MAXIMIZED_HORIZ) != 0) {
                    MainWindow.this.setSplitSizes();
                }
            }
         });

        setMinimumSize(new Dimension(1024, 768));
        setPreferredSize(new Dimension(1280, 1024));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                try {
                    LOGGER.info("Saving config file...");
                    DbConfigFile.saveConfig();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Unable to save config file.", ex);
                }
                System.exit(0);
            }
        });

        getContentPane().setLayout(new BorderLayout());

        //!\ bug with multi screen : cannot drag window to another screen
        if (DbConfigFile.getInstance().getDecoratedWindow()) {
            setUndecorated(true);
            getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.WHITE));
            RibbonWindowUI ribbonWinUI = new RibbonWindowUI();
            JLayer<Ribbon> ribbonWithWinIcons = new JLayer<>(getRibbon(), ribbonWinUI);
            ribbonWithWinIcons.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
            getContentPane().add(ribbonWithWinIcons, BorderLayout.PAGE_START);
        } else {
            getContentPane().add(getRibbon(), BorderLayout.PAGE_START);
        }

        getContentPane().add(getSplitMain(), BorderLayout.CENTER);
        getContentPane().add(getStatusBar(), BorderLayout.AFTER_LAST_LINE);

        /* change this to add later quick access toolbar
        JMenu menu = new JMenu("File");
        JMenuItem menuFileExit = new JMenuItem(appActions.get("Exit"));
        menu.add(menuFileExit);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);
         */
    }

    private void setSplitSizes() {
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(50);

                Dimension size = MainWindow.getInstance().getSize();
                getSplitMain().setDividerLocation(size.width / 4);

                size = getSplitMain().getRightComponent().getSize();
                getSplitEditor().setDividerLocation(size.width * 5 / 6);
            } catch (Exception ex) {
                //ignored
            }
        });
    }

    private void addAction(AbstractRibbonAction action) {
        if (action.getValue(Action.NAME) == null) {
            LOGGER.warning("Action has no name: " + action.getClass().getName());
        } else if (appActions.get((String)action.getValue(Action.NAME)) != null) {
            LOGGER.warning("There is already an action named: " + action.getValue(Action.NAME) + " defined.");
        } else {
            appActions.put((String)action.getValue(Action.NAME), action); 
        }
    }

    private void initActions() {
        appActions.clear();

        addAction(new ShowConfigPanelAction());
        addAction(new ExitAction());
        addAction(new ShowQueryPanelAction());
    }

    /**
     * Gets the application ribbon to interact with
     * @return the Ribbon of the application
     */
    public Ribbon getRibbon() {
        if(this.ribbon == null) {
            this.ribbon = new Ribbon();
            this.ribbon.setOpaque(true);
            //this.ribbon.setBackground(Color.WHITE);
            this.ribbon.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
            this.ribbon.addRibbonTab(getHomeRibbonTab());
            this.ribbon.addRibbonTab(getExplorerRibbonTab());
            this.ribbon.addRibbonTab(getEditorRibbonTab());
            this.ribbon.addRibbonTab(getVisualDbRibbonTab());
            this.ribbon.addRibbonTab(getBatchRibbonTab());
        }

        return this.ribbon;
    }

    private RibbonTab getExplorerRibbonTab() {
        if (explorerRibbonTab == null) {
            explorerRibbonTab = new DbExplorerRibbonTab();
        }
        return explorerRibbonTab;
    }

    private RibbonTab getVisualDbRibbonTab() {
        if (this.visualDbRibbonTab == null) {
            this.visualDbRibbonTab = new VisualDbRibbonTab();
        }
        return this.visualDbRibbonTab;
    }

    private RibbonTab getHomeRibbonTab() {
        if (this.homeRibbonTab == null) {
            this.homeRibbonTab = new RibbonTab("Home");
            this.homeRibbonTab.addGroup(getFileRibbonGroup());
            this.homeRibbonTab.addGroup(getToolsRibbonGroup());
            this.homeRibbonTab.addGroup(getSystemRibbonGroup());
        }

        return this.homeRibbonTab;
    }

    private RibbonTab getEditorRibbonTab() {
        if (editorRibbonTab == null) {
            editorRibbonTab = new EditorRibbonTab();
        }

        return editorRibbonTab;
    }

    private RibbonTab getBatchRibbonTab() {
        if (batchRibbonTab == null) {
            batchRibbonTab = new BatchRibbonTab();
        }

        return batchRibbonTab;
    }

    private RibbonGroup getFileRibbonGroup() {
        if (fileRibbonGroup == null) {
            this.fileRibbonGroup = new FileRibbonGroup();
        }
        return this.fileRibbonGroup;
    }

    private RibbonGroup getToolsRibbonGroup() {
        if (toolsRibbonGroup == null) {
            toolsRibbonGroup = new RibbonGroup("Tools");
            toolsRibbonGroup.addButton(new NewVisualDbTabAction(), RibbonGroup.LARGE);
            toolsRibbonGroup.addButton(new NewBatchAction(), RibbonGroup.LARGE);
        }

        return toolsRibbonGroup;
    }

    private RibbonGroup getSystemRibbonGroup() {
        if (this.systemRibbonGroup == null) {
            this.systemRibbonGroup = new RibbonGroup("System");

            this.systemRibbonGroup.addButton(appActions.get(ShowConfigPanelAction.NAME), RibbonGroup.LARGE);
            this.systemRibbonGroup.addButton(new ShowLogAction(), RibbonGroup.LARGE);
            this.systemRibbonGroup.addButton(appActions.get(ExitAction.NAME), RibbonGroup.LARGE);
        }

        return this.systemRibbonGroup;
    }

    private JPanel getStatusBar() {
        if (pnlStatusBar == null) {
            pnlStatusBar = new JPanel();
            pnlStatusBar.setOpaque(true);
            pnlStatusBar.setBackground(Color.WHITE);

            pnlStatusBar.setLayout(new BorderLayout());

            pnlStatusBar.add(getLblConnectionStatus(), BorderLayout.WEST);
            pnlStatusBar.add(new JLabel("version " + App.getVersion()), BorderLayout.EAST);
        }

        return pnlStatusBar;
    }

    private JLabel getLblConnectionStatus() {
        if (lblConnectionStatus == null) {
            lblConnectionStatus = new JLabel();
            lblConnectionStatus.setText("Welcome to MiniSQL by Hironico.com");
        }

        return lblConnectionStatus;
    }

    private JSplitPaneNoDivider getSplitMain() {
        if (splitMain == null) {
            splitMain = new JSplitPaneNoDivider();
            splitMain.setLeftComponent(this.getTabExplorer());
            splitMain.setRightComponent(this.getSplitEditor());
            splitMain.setDividerSize(7);
            splitMain.setDividerLocation(0.15d);
            splitMain.setBorder(BorderFactory.createEmptyBorder());
        }

        return splitMain;
    }

    private JTabbedPaneNoContentSeparator getTabExplorer() {
        if (this.tabExplorer == null) {
            this.tabExplorer = new JTabbedPaneNoContentSeparator();
            this.tabExplorer.setBorder(BorderFactory.createEmptyBorder(0,10,5,5));
            this.tabExplorer.addTab("Explorer", getSchemaExplorerPanel());
        }

        return this.tabExplorer;
    }

    public SchemaExplorerPanel getSchemaExplorerPanel() {
        if (this.schemaExplorerPanel == null) {
            this.schemaExplorerPanel = new SchemaExplorerPanel();
        }

        return this.schemaExplorerPanel;
    }


    private JSplitPaneNoDivider getSplitEditor() {
        if (splitEditor == null) {
            splitEditor = new JSplitPaneNoDivider();
            splitEditor.setLeftComponent(getTabEditors());
            splitEditor.setRightComponent(getTabTools());
            splitEditor.setDividerSize(7);
            splitEditor.setDividerLocation(0.75d);
            splitEditor.setBorder(BorderFactory.createEmptyBorder());
        }

        return splitEditor;
    }

    private JTabbedPaneNoContentSeparator getTabTools() {
        if (tabTools == null) {
            tabTools = new JTabbedPaneNoContentSeparator();
            tabTools.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            tabTools.addTab("History", getPnlHistory());
        }

        return tabTools;
    }

    private QueryHistoryPanel getPnlHistory() {
        if (pnlHistory == null) {
            pnlHistory = new QueryHistoryPanel();
        }

        return pnlHistory;
    }

    private JTabbedPaneNoContentSeparator getTabEditors() {
        if (tabEditors == null) {
            tabEditors = new JTabbedPaneNoContentSeparator();

            tabEditors.addTab("SQL query", new QueryPanel());

            // tabEditors.setBorder(BorderFactory.createEmptyBorder(-3, 0, -2, -3));

            tabEditors.addChangeListener(evt -> {
                Component comp = tabEditors.getWrappedSelectedComponent();
                if (comp instanceof QueryPanel queryPanel) {
                    queryPanel.updateRibbon();
                }
                if (comp instanceof BatchPanel batchPanel) {
                    batchPanel.updateRibbon();
                }
                if (comp instanceof VisualDbPanel visualDbPanel) {
                    visualDbPanel.updateRibbon();
                }
            });
        }

        return tabEditors;
    }

    /**
     * Searches for the given title and returns trus if it finds at leat one in the editor tabs.
     * @param title the title we want to check it is existing in the editor abs
     * @return true isf the editor tabs have one given title, false if not found.
     */
    public boolean hasOneEditorNamed(String title) {
        for (int index = 0; index < getTabEditors().getTabCount(); index++) {
            if (title.equalsIgnoreCase(getTabEditors().getTitleAt(index))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Selects the first editor tab whose title is equals to the given title.
     * Does nothing if title is not found.
     * @param title the title for tab component when want to select
     */
    public void setSelectedEditor(String title) {
        for (int index = 0; index < getTabEditors().getTabCount(); index++) {
            if (title.equalsIgnoreCase(getTabEditors().getTitleAt(index))) {
                getTabEditors().setSelectedIndex(index);
                return;
            }
        }
    }

    /**
     * Add a new tab in the editor's area with the provided components and title to display.
     * @param comp the component to display in the editors tab
     * @param title the tab title
     */
    public void addNewEditorTab(JComponent comp, String title) {
        Runnable run = () -> {
            JTabbedPane tabResults = getTabEditors();
            tabResults.add(comp, title);
            tabResults.setSelectedIndex(tabResults.getTabCount() - 1);
            tabResults.setTabComponentAt(tabResults.getTabCount() - 1, new CloseableTabComponent(tabResults, title));

            if (comp instanceof QueryPanel queryPanel) {
                queryPanel.updateRibbon();
            }
        };

        SwingUtilities.invokeLater(run);
    }

    /**
     * Removes the current editor tab. If no selection then does nothing.
     */
    public void removeCurrentEditorTab() {
        int selectedIndex = getTabEditors().getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        getTabEditors().removeTabAt(selectedIndex);
    }

    /**
     * Defines the title of the given editor component. It searches for the component in the editor tabs
     * and set the title for the first encountered. If not found does nothing.
     * @param comp the Component in the editor tabs when want to set the title for the tab.
     * @param title title to set.
     */
    public void setEditorTabTitle(JComponent comp, String title) {
        int count = getTabEditors().getTabCount();
        for (int index = 0; index < count; index++) {
            Component myComp = getTabEditors().getComponentAt(index);
            if (myComp.equals(comp)) {
                Component tc = getTabEditors().getTabComponentAt(index);
                CloseableTabComponent ctc = (CloseableTabComponent) tc;
                ctc.setTitle(title);
                return;
            }
        }
    }

    /**
     * Get the component of the currently selected editor tab
     * @return Component instance of the current editor tab, and null if none selected
     */
    public Component getCurrentEditorTabComponent() {
        int selectedIndex = getTabEditors().getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        }

        return getTabEditors().getComponentAt(selectedIndex);
    }

    /**
     * Returns the index of the first editor tab with given title. Can be selected automatically if found.
     * @param title the title with search for an editor
     * @param autoSelect set to true to select the found editor if any
     * @return index of the editor whose title is equals to the given one and -1 if not found
     */
    public int getEditorTabIndexOf(String title, boolean autoSelect) {
        for (int index = 0; index < getTabEditors().getTabCount(); index++) {
            if (title.equals(getTabEditors().getTitleAt(index))) {
                if (autoSelect) {
                    getTabEditors().setSelectedIndex(index);
                }
                return index;
            }
        }

        return -1;
    }
}