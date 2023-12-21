/*
 * VisualDbPanel.java
 *
 * Created on Dec 24, 2009, 12:05:57 PM
 */
package net.hironico.minisql.ui.visualdb;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import com.jidesoft.swing.JideScrollPane;
import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.model.SQLTable;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.tableselector.ShowTableSelectorAction;

import org.jdesktop.jxlayer.JXLayer;
import org.netbeans.api.visual.widget.BirdViewController;
import org.netbeans.modules.visual.widget.SatelliteComponent;

/**
 * Le VisualDbPanel est l'outil de modélisation graphique de la base de données.
 * Pour la version 2.1.0 c'est un outil en lecture seule mais il sera amené à évoluer
 * pour devenir un composant central de creation et de modification des tables.
 * Ce panel utilise la Visual Library de Netbeans pour créer une scene de modélisation
 * avec tout le confort moderne. D'autre part ce panel dispose d'une petite palette pour
 * la navigation dans les grands modèles (zoom, bird view et satellite view) ainsi que
 * la possibilité de filtrer les tables affichées ainsi que le niveau de détail.
 * @author hironico
 * @since 2.1.0
 */
public class VisualDbPanel extends javax.swing.JPanel implements DbConfigFile.DbConfigFileListener {

    protected static final Logger logger = Logger.getLogger(VisualDbPanel.class.getName());
    private SatelliteComponent satelliteComponent = null;
    private BirdViewController currentBirdView = null;
    private DBGraphScene graphScene = null;
    private JButton btnNavigation = new JButton();
    private JXLayer<JComponent> sceneLayer = null;

    /** Creates new form VisualDbPanel */
    public VisualDbPanel() {
        initComponents();
        loadIcons();

        // quelques améliorations de la gui pour JGoodies look
        /*
        mainToolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
        mainToolBar.putClientProperty(Plastic3DLookAndFeel.BORDER_STYLE_KEY, BorderStyle.SEPARATOR);
        */
        refreshConnectionNames();

        graphScene = new DBGraphScene();

        // pour etre notifié de l'ajout / suppression des connexions dans la combo des connexions
        DbConfigFile.addListener(this);

        // setup des actions pour les boutons de la barre des taches.
        btnImageExport.addActionListener(new ExportSceneImageToFileAction(graphScene));
        btnImageCopy.addActionListener(new ExportSceneImageToClipboardAction(graphScene));

        // setup pour la navigation

        scrollScene.setViewportView(graphScene.createView());
        final SatelliteUI satelliteUI = new SatelliteUI(graphScene);
        sceneLayer = new JXLayer<JComponent>(scrollScene, satelliteUI);
        this.add(sceneLayer, BorderLayout.CENTER);

        btnNavigation.setText("+");
        btnNavigation.setToolTipText("Click here to open the navigation satellite pane !");
        btnNavigation.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        btnNavigation.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                satelliteUI.setSatellitevisible(!satelliteUI.isSatelliteVisible());
            }
        });
        scrollScene.setScrollBarCorner(JideScrollPane.VERTICAL_TOP, btnNavigation);

        // setup pour la progress bar en bas de la scene.
        collapsProgressPanel.setCollapsed(true);
        collapsProgressPanel.add(pnlProgress);
    }

    /**
     * Permet de mettre à jour la liste des connexions disponibles dans la combo
     * SANS déclencher le itemStateChanged, sauf si le pool actuellement sélectionné
     * a été déconnecté.
     * @since 2.1.0
     */
    public void refreshConnectionNames() {
        Collection<String> names = DbConfigFile.getConfigNames();

        names.forEach(name -> {
            if (!comboNamesContains(name)) {
                cmbConnectionName.addItem(name);
            }
        });

        for (int cpt = 0; cpt < cmbConnectionName.getItemCount(); cpt++) {
            String myName = (String) cmbConnectionName.getModel().getElementAt(cpt);
            if (!names.contains(myName)) {
                cmbConnectionName.removeItem(myName);
            }
        }
    }

    protected boolean comboNamesContains(String name) {
        for (int cpt = 0; cpt < cmbConnectionName.getItemCount(); cpt++) {
            String myName = (String) cmbConnectionName.getModel().getElementAt(cpt);
            if (myName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void configAdded(DbConfig cfg) {
        refreshConnectionNames();
    }

    @Override
    public void configRemoved(DbConfig cfg) {
        refreshConnectionNames();
    }

    protected void loadIcons() {
        /*
        ImageIcon icon = ImageCache.getInstance().loadImageIcon("org/hironico/resource/icons/inconexperience/small/shadow/table_replace.png");
        if (icon == null) {
            logger.error("Cannot load icon : org/hironico/resource/icons/inconexperience/small/shadow/table_replace.png");
        } else {
            btnSelectTables.setIcon(icon);
            btnSelectTables.setText("");
        }

        icon = ImageCache.getInstance().loadImageIcon("org/hironico/resource/icons/inconexperience/small/shadow/layout_center.png");
        if (icon == null) {
            logger.error("Cannot load icon : org/hironico/resource/icons/inconexperience/small/shadow/layout_center.png");
        } else {
            btnAutoLayout.setIcon(icon);
            btnAutoLayout.setText("");
        }

        icon = ImageCache.getInstance().loadImageIcon("org/hironico/resource/icons/inconexperience/small/shadow/zoom_in.png");
        if (icon == null) {
            logger.error("Cannot load icon : org/hironico/resource/icons/inconexperience/small/shadow/zoom_in.png");
        } else {
            btnZoomPlus.setIcon(icon);
            btnZoomPlus.setText("");
        }

        icon = ImageCache.getInstance().loadImageIcon("org/hironico/resource/icons/inconexperience/small/shadow/zoom_out.png");
        if (icon == null) {
            logger.error("Cannot load icon : org/hironico/resource/icons/inconexperience/small/shadow/zoom_out.png");
        } else {
            btnZoomMinus.setIcon(icon);
            btnZoomMinus.setText("");
        }

        icon = ImageCache.getInstance().loadImageIcon("org/hironico/resource/icons/inconexperience/small/shadow/view_1_1.png");
        if (icon == null) {
            logger.error("Cannot load icon : org/hironico/resource/icons/inconexperience/small/shadow/view_1_1.png");
        } else {
            btnZoomOriginal.setIcon(icon);
            btnZoomOriginal.setText("");
        }

        icon = ImageCache.getInstance().loadImageIcon("org/hironico/resource/icons/inconexperience/small/shadow/disk_blue.png");
        if (icon == null) {
            logger.error("Cannot load icon : org/hironico/resource/icons/inconexperience/small/shadow/disk_bleu.png");
        } else {
            btnImageExport.setIcon(icon);
            btnImageExport.setText("");
        }

        icon = ImageCache.getInstance().loadImageIcon("org/hironico/resource/icons/inconexperience/small/shadow/photo_scenery.png");
        if (icon == null) {
            logger.severe("Cannot load icon : org/hironico/resource/icons/inconexperience/small/shadow/photo_scenery.png");
        } else {
            btnImageCopy.setIcon(icon);
            btnImageCopy.setText("");
        }

        icon = ImageCache.getInstance().loadImageIcon("org/hironico/resource/icons/inconexperience/small/shadow/eyeglasses.png");
        if (icon == null) {
            logger.severe("Cannot load icon : org/hironico/resource/icons/inconexperience/small/shadow/eyeglasses.png");
        } else {
            toggleMangifyView.setIcon(icon);
            toggleMangifyView.setText("");
        }
         */
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        scrollScene = new com.jidesoft.swing.JideScrollPane();
        pnlProgress = new javax.swing.JPanel();
        lblProgress = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        mainToolBar = new javax.swing.JToolBar();
        toggleMangifyView = new javax.swing.JToggleButton();
        btnZoomPlus = new javax.swing.JButton();
        btnZoomMinus = new javax.swing.JButton();
        btnZoomOriginal = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnImageCopy = new javax.swing.JButton();
        btnImageExport = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnAutoLayout = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnSelectTables = new javax.swing.JButton();
        cmbConnectionName = new javax.swing.JComboBox();
        collapsProgressPanel = new org.jdesktop.swingx.JXCollapsiblePane();

        scrollScene.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        pnlProgress.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlProgress.setLayout(new java.awt.GridBagLayout());

        lblProgress.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblProgress.setText("Please wait while data is retreived for display :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlProgress.add(lblProgress, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        pnlProgress.add(progressBar, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());

        mainToolBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainToolBar.setFloatable(false);
        mainToolBar.setRollover(true);

        toggleMangifyView.setText("Magnify view");
        toggleMangifyView.setToolTipText("When zoomed out, use to magnify view to quickly find a table");
        toggleMangifyView.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                toggleMangifyViewStateChanged(evt);
            }
        });
        mainToolBar.add(toggleMangifyView);

        btnZoomPlus.setText("Zoom +");
        btnZoomPlus.setToolTipText("Hold CTRL and use mouse well to zoom in/out !");
        btnZoomPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomPlusActionPerformed(evt);
            }
        });
        mainToolBar.add(btnZoomPlus);

        btnZoomMinus.setText("Zoom -");
        btnZoomMinus.setToolTipText("Hold CTRL and use mouse well to zoom in/out !");
        btnZoomMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomMinusActionPerformed(evt);
            }
        });
        mainToolBar.add(btnZoomMinus);

        btnZoomOriginal.setText("Zoom 1:1");
        btnZoomOriginal.setToolTipText("Hold CTRL and use mouse well to zoom in/out !");
        btnZoomOriginal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomOriginalActionPerformed(evt);
            }
        });
        mainToolBar.add(btnZoomOriginal);
        mainToolBar.add(jSeparator3);

        btnImageCopy.setText("Copy as image");
        btnImageCopy.setFocusable(false);
        btnImageCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnImageCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(btnImageCopy);

        btnImageExport.setText("Export image");
        btnImageExport.setFocusable(false);
        btnImageExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnImageExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mainToolBar.add(btnImageExport);
        mainToolBar.add(jSeparator2);

        btnAutoLayout.setText("Auto layout");
        btnAutoLayout.setToolTipText("Tries to layout the tables automatically so that the diagram is more human readable.");
        btnAutoLayout.setFocusable(false);
        btnAutoLayout.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAutoLayout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAutoLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAutoLayoutActionPerformed(evt);
            }
        });
        mainToolBar.add(btnAutoLayout);
        mainToolBar.add(jSeparator1);

        btnSelectTables.setText("Select tables");
        btnSelectTables.setFocusable(false);
        btnSelectTables.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelectTables.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelectTables.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectTablesActionPerformed(evt);
            }
        });
        mainToolBar.add(btnSelectTables);

        cmbConnectionName.setMinimumSize(new java.awt.Dimension(100, 20));
        cmbConnectionName.setPreferredSize(new java.awt.Dimension(100, 22));
        mainToolBar.add(cmbConnectionName);

        add(mainToolBar, java.awt.BorderLayout.NORTH);

        collapsProgressPanel.setDoubleBuffered(true);
        add(collapsProgressPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectTablesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectTablesActionPerformed
        String connectionName = (String) cmbConnectionName.getSelectedItem();
        if ((connectionName == null) || "".equals(connectionName.trim())) {
            JOptionPane.showMessageDialog(VisualDbPanel.this,
                    "Please connect first!",
                    "Hey!",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ShowTableSelectorAction selectionAction = new ShowTableSelectorAction(this, connectionName);
        selectionAction.setTitle("Select the tables to display");
        selectionAction.setSubTitle("Use the list below to select the table to display in the visual database tool. Right clic the table for selection tools.");
        selectionAction.setSelectedTablesList(graphScene.getDisplayedTableList());
        selectionAction.actionPerformed(null);

        // verif si l'utilisateur a cliqué sur ok ou cancel
        if (selectionAction.getUserClickButton() != JOptionPane.OK_OPTION) {
            return;
        }

        DbConfig dbConfig = DbConfigFile.getConfig(connectionName);
        final List<SQLTable> tables = selectionAction.getSelectedTablesList();

        // recup des colonnes des tables en asynchrone
        SQLTableLoaderThread loaderThread = new SQLTableLoaderThread(tables, dbConfig);
        Future<List<SQLTable>> fut = MainWindow.executorService.submit(loaderThread);

        try {
            graphScene.cleanUpScene();
            graphScene.createScene(fut.get());
            graphScene.revalidate();
            graphScene.validate();
        } catch (InterruptedException | ExecutionException ie) {
            ie.printStackTrace();
        }
    }

    private void toggleMangifyViewStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_toggleMangifyViewStateChanged
        if (graphScene == null) {
            return;
        }

        if (toggleMangifyView.isSelected()) {
            if (currentBirdView == null) {
                currentBirdView = graphScene.createBirdView();
                currentBirdView.setZoomFactor(1.0d);
            }

            currentBirdView.show();
        } else {
            if (currentBirdView != null) {
                currentBirdView.hide();
            }
        }
    }//GEN-LAST:event_toggleMangifyViewStateChanged

    private void btnAutoLayoutActionPerformed(java.awt.event.ActionEvent evt) {
        if (graphScene == null) {
            return;
        }

        if (graphScene.getEdges().size() == 0) {
            int confirm = JOptionPane.showConfirmDialog(VisualDbPanel.this,
                    "There is no foreign key defined in this model.\n"
                    + "Tables will be laid out horizontaly.\n"
                    + "For a large number of table this could not be optimal.\n"
                    + "Do the layout anyway ?",
                    "Warning...",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        SwingUtilities.invokeLater(() -> {
            graphScene.layoutScene();
            graphScene.revalidate ();
            graphScene.validate ();
        });
    }

    private void btnZoomPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomPlusActionPerformed
        double newZoomFactor = graphScene.getZoomFactor() * 1.10d;
        graphScene.setZoomFactor(newZoomFactor);
        sceneLayer.repaint();
    }//GEN-LAST:event_btnZoomPlusActionPerformed

    private void btnZoomMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomMinusActionPerformed
        double newZoomFactor = graphScene.getZoomFactor() * 0.90d;
        graphScene.setZoomFactor(newZoomFactor);
        sceneLayer.repaint();
    }//GEN-LAST:event_btnZoomMinusActionPerformed

    private void btnZoomOriginalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomOriginalActionPerformed
        graphScene.setZoomFactor(1.0d);
        sceneLayer.repaint();
    }//GEN-LAST:event_btnZoomOriginalActionPerformed

    /**
     * Cree le satellite de navigation pour le diagramme affiché à l'écran
     * @return SatelliteComponent qui correspond à la DBGraphScene que l'on vient de créer.
     * @since 2.1.0
     */
    protected SatelliteComponent getSatelliteComponent() {
        if (graphScene == null) {
            return null;
        }

        if (satelliteComponent == null) {
            satelliteComponent = (SatelliteComponent) graphScene.createSatelliteView();
            satelliteComponent.setPreferredSize(new Dimension(128, 128));
            satelliteComponent.setMinimumSize(new Dimension(128, 128));
            satelliteComponent.setMaximumSize(new Dimension(128, 128));
            satelliteComponent.addNotify();
            // pnlSatellite.add(satelliteComponent, BorderLayout.CENTER);
        }
        return satelliteComponent;
    }

    public void showVisualDb(List<SQLTable> tableList) {
        if (currentBirdView != null) {
            currentBirdView.hide();
        }

        graphScene.cleanUpScene();
        graphScene.createScene(tableList);

        if (graphScene.getEdges().size() > 0) {
            graphScene.layoutScene();
        } else {
            logger.warning("No auto layout since there is no edge in this scene.");
        }

        toggleMangifyViewStateChanged(null);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAutoLayout;
    private javax.swing.JButton btnImageCopy;
    private javax.swing.JButton btnImageExport;
    private javax.swing.JButton btnSelectTables;
    private javax.swing.JButton btnZoomMinus;
    private javax.swing.JButton btnZoomOriginal;
    private javax.swing.JButton btnZoomPlus;
    private javax.swing.JComboBox<String> cmbConnectionName;
    private org.jdesktop.swingx.JXCollapsiblePane collapsProgressPanel;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JLabel lblProgress;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JPanel pnlProgress;
    private javax.swing.JProgressBar progressBar;
    private com.jidesoft.swing.JideScrollPane scrollScene;
    private javax.swing.JToggleButton toggleMangifyView;
    // End of variables declaration//GEN-END:variables
}
