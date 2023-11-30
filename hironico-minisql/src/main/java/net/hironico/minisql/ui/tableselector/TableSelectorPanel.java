/*
 * TableSelectorPanel.java
 *
 * Created on Feb 20, 2010, 2:50:05 PM
 */
package net.hironico.minisql.ui.tableselector;

import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ctrl.ObjectListCallable;
import net.hironico.minisql.model.SQLObjectTypeEnum;
import net.hironico.minisql.model.SQLTable;
import net.hironico.minisql.ui.MainWindow;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Ce panel permet la sélection des tables pour une connexion donnée.
 * @author hironico
 * @since 2.1.0
 * @see #updateTableList(java.lang.String)
 */
public class TableSelectorPanel extends JPanel {

    protected static final Logger LOGGER = Logger.getLogger(TableSelectorPanel.class.getName());
    /**
     * Permet de savoir quel bouton a été cliqué par l'utilisateur.
     * @since 2.1.0
     */
    protected int userClickButton = JOptionPane.CANCEL_OPTION;

    /**
     * Liste des tables sélectionnées par l'utilisateur. On peut mettre à
     * jour cette property et la préselection est faite lors du refresh.
     * @see #updateTableList(java.lang.String)
     * @see #isSelectedTable(SQLTable)
     * @since 2.1.0
     */
    protected List<SQLTable> selectedTablesList = null;

    /** Creates new form TableSelectorPanel */
    public TableSelectorPanel() {
        initComponents();

        // extra setup pour la banner
        bannerPanel.setEndColor(TableSelectorPanel.this.getBackground());

        // extra setup pour la table de sélection
        tableSlectedTables.setHighlighters(HighlighterFactory.createAlternateStriping());
    }

    public void setTitle(String messageTitle) {
        bannerPanel.setTitle(messageTitle);
    }

    public void setSubTitle(String subTitle) {
        bannerPanel.setSubtitle(subTitle);
    }

    /**
     * Méthode importante permettant de savoir sur quel bouton a clické
     * l'utilisateur lorsqu'il a voulu fermer la fenêtre (si le panel
     * est bien dans une fenetre). Soit c'est JOptionPane.CANCEL_OPTION, soit
     * JOptionPane.OK_OPTION
     * @return soit JOptionPane.CANCEL_OPTION, soit JOptionPane.OK_OPTION
     * @since 2.1.0
     */
    public int getUserClickButton() {
        return userClickButton;
    }

    /**
     * Permet de fermer la fenetre parent de ce panel.
     * Attention, si ce panel n'est pas dans une JDialog
     * alors cela ferme l'application !
     * @since 2.1.0
     */
    protected void closeDialog() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) {
            win.setVisible(false);
            win.dispose();
        }
    }

    /**
     * Permet de lancer la récup des noms de tables dans un thread séparé
     * car s'il y a beaucoup de tables à récupérer ça peut etre long.
     * Pendant ce temps la liste affiche un item indiquant que la récup est
     * en cours.
     * @param connectionName le nom de la connexion pour le chargement des tables.
     * @since 2.1.0
     */
    public void updateTableList(String connectionName) {
        if ((connectionName == null) || "".equals(connectionName.trim())) {
            LOGGER.severe("Cannot get the table list from a null or empty connection name.");
            return;
        }

        lblActiveConnectionName.setText(connectionName);

        ObjectListCallable call = new ObjectListCallable(DbConfigFile.getConfig(connectionName),
                "public", SQLObjectTypeEnum.TABLE);

        this.clearDisplayedTables();
        Future<List<String[]>> fut = MainWindow.executorService.submit(call);

        try {
            this.displayTables(fut.get());
        } catch (InterruptedException | ExecutionException ie) {
            LOGGER.log(Level.SEVERE, "Cannot get table list.", ie);
        }
    }

    public void clearDisplayedTables() {
        DefaultTableModel model = (DefaultTableModel) tableSlectedTables.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        Object[] row = new Object[2];
        row[0] = Boolean.FALSE;
        row[1] = "Please wait while tables are retreived";
        model.addRow(row);

        tableSlectedTables.setEnabled(false);
    }

    /**
     * Permet de savoir si une table est dans la liste des tables sélectionnées.
     * Utile pour rétablir une sélection après une utilisation précédente du sélecteur.
     * @param theTable la table a tester pour savoir si on la préselectionne ou pas
     * @return true si la table fait partie de la préselection et false sinon.
     * @since 2.1.0
     */
    protected boolean isSelectedTable(SQLTable theTable) {
        if (selectedTablesList == null) {
            return false;
        }

        for (SQLTable selectedTable : selectedTablesList) {
            if (theTable.name.equals(selectedTable.name)) {
                return true;
            }
        }

        return false;
    }

    public void displayTables(List<String[]> tables) {
        DefaultTableModel model = (DefaultTableModel) tableSlectedTables.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        for (String[] myObj : tables) {
            SQLTable myTable = new SQLTable(myObj[0], myObj[1]);
            Object[] row = new Object[2];
            row[0] = isSelectedTable(myTable);
            row[1] = myTable;
            model.addRow(row);
        }

        tableSlectedTables.setEnabled(true);
    }

    /**
     * Permet de sélectionner les tables fournies en paramètre. On va cocher
     * toute table présente dans la liste dans la GUI. La pré-selection
     * se fait lors du refresh des tables disponibles.
     * @param selectedTables liste des tables à sélectionner dans la GUI.
     * @see #updateTableList(java.lang.String)
     * @since 2.1.0
     */
    public void setSelectedTablesList(List<SQLTable> selectedTables) {
        this.selectedTablesList = selectedTables;
    }

    /**
     * Permet d'obtenir la liste des SQLtable qui ont été sélectionnées dans
     * l'interface graphique.
     * @return List<SQLTable> sélectionnées par l'utilisateur.
     * @since 2.1.0
     */
    public List<SQLTable> getSelectedTablesList() {
        DefaultTableModel model = (DefaultTableModel) tableSlectedTables.getModel();
        selectedTablesList = new ArrayList<SQLTable>();
        for (int row = 0; row < model.getRowCount(); row++) {
            if ((Boolean) model.getValueAt(row, 0)) {
                selectedTablesList.add((SQLTable) model.getValueAt(row, 1));
            }
        }
        return selectedTablesList;
    }

    protected void showPopupMenu(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        menuTickAll = new javax.swing.JMenuItem();
        menuTickNone = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuTickSelection = new javax.swing.JMenuItem();
        menuUnTickSelection = new javax.swing.JMenuItem();
        bannerPanel = new com.jidesoft.dialog.BannerPanel();
        pnlCommands = new javax.swing.JPanel();
        lblActiveConnection = new javax.swing.JLabel();
        lblActiveConnectionName = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();
        scrollSelectedTables = new javax.swing.JScrollPane();
        tableSlectedTables = new org.jdesktop.swingx.JXTable();

        menuTickAll.setText("Tick all");
        menuTickAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTickAllActionPerformed(evt);
            }
        });
        popupMenu.add(menuTickAll);

        menuTickNone.setText("Tick none");
        menuTickNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTickNoneActionPerformed(evt);
            }
        });
        popupMenu.add(menuTickNone);
        popupMenu.add(jSeparator1);

        menuTickSelection.setText("Tick selection");
        menuTickSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuTickSelectionActionPerformed(evt);
            }
        });
        popupMenu.add(menuTickSelection);

        menuUnTickSelection.setText("Untick selection");
        menuUnTickSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuUnTickSelectionActionPerformed(evt);
            }
        });
        popupMenu.add(menuUnTickSelection);

        setLayout(new java.awt.BorderLayout());

        bannerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        bannerPanel.setStartColor(java.awt.Color.white);
        bannerPanel.setSubtitle("Use the list below to select the tables.");
        bannerPanel.setTitle("Select the tables");
        add(bannerPanel, java.awt.BorderLayout.PAGE_START);

        pnlCommands.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlCommands.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        lblActiveConnection.setText("Ative connection is:");
        pnlCommands.add(lblActiveConnection);

        lblActiveConnectionName.setText("NONE");
        pnlCommands.add(lblActiveConnectionName);

        btnCancel.setText("Cancel");
        btnCancel.setPreferredSize(new java.awt.Dimension(75, 23));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        pnlCommands.add(btnCancel);

        btnOK.setText("OK");
        btnOK.setPreferredSize(new java.awt.Dimension(75, 23));
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        pnlCommands.add(btnOK);

        add(pnlCommands, java.awt.BorderLayout.SOUTH);

        tableSlectedTables.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Selected?", "Table name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableSlectedTables.setColumnControlVisible(true);
        tableSlectedTables.setDoubleBuffered(true);
        tableSlectedTables.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableSlectedTablesMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableSlectedTablesMouseReleased(evt);
            }
        });
        scrollSelectedTables.setViewportView(tableSlectedTables);

        add(scrollSelectedTables, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        userClickButton = JOptionPane.OK_OPTION;
        closeDialog();
    }//GEN-LAST:event_btnOKActionPerformed

    private void menuTickAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuTickAllActionPerformed
        for (int row = 0; row < tableSlectedTables.getRowCount(); row++) {
            tableSlectedTables.setValueAt(Boolean.TRUE, row, 0);
        }
    }//GEN-LAST:event_menuTickAllActionPerformed

    private void menuTickNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuTickNoneActionPerformed
        for (int row = 0; row < tableSlectedTables.getRowCount(); row++) {
            tableSlectedTables.setValueAt(Boolean.FALSE, row, 0);
        }
    }//GEN-LAST:event_menuTickNoneActionPerformed

    private void menuTickSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuTickSelectionActionPerformed
        int[] indexes = tableSlectedTables.getSelectedRows();
        for (int row : indexes) {
            tableSlectedTables.setValueAt(Boolean.TRUE, row, 0);
        }
    }//GEN-LAST:event_menuTickSelectionActionPerformed

    private void menuUnTickSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuUnTickSelectionActionPerformed
        int[] indexes = tableSlectedTables.getSelectedRows();
        for (int row : indexes) {
            tableSlectedTables.setValueAt(Boolean.FALSE, row, 0);
        }
    }//GEN-LAST:event_menuUnTickSelectionActionPerformed

    private void tableSlectedTablesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableSlectedTablesMouseReleased
        showPopupMenu(evt);
    }//GEN-LAST:event_tableSlectedTablesMouseReleased

    private void tableSlectedTablesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableSlectedTablesMousePressed
        showPopupMenu(evt);
    }//GEN-LAST:event_tableSlectedTablesMousePressed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        userClickButton = JOptionPane.CANCEL_OPTION;
        closeDialog();
    }//GEN-LAST:event_btnCancelActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.jidesoft.dialog.BannerPanel bannerPanel;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JLabel lblActiveConnection;
    private javax.swing.JLabel lblActiveConnectionName;
    private javax.swing.JMenuItem menuTickAll;
    private javax.swing.JMenuItem menuTickNone;
    private javax.swing.JMenuItem menuTickSelection;
    private javax.swing.JMenuItem menuUnTickSelection;
    private javax.swing.JPanel pnlCommands;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JScrollPane scrollSelectedTables;
    private org.jdesktop.swingx.JXTable tableSlectedTables;
    // End of variables declaration//GEN-END:variables
}
