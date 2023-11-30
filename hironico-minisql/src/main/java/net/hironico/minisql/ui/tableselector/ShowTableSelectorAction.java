/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hironico.minisql.ui.tableselector;

import net.hironico.common.swing.dialog.OpenDialogAction;
import net.hironico.minisql.model.SQLTable;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JComponent;

/**
 * Permet de montrer une JDialog avec un selecteur de tables dans la dialog.
 * @author hironico
 * @since 2.1.0
 */
public class ShowTableSelectorAction extends OpenDialogAction {

    /**
     * Le nom de la connexion pour charger les tables.
     * @since 2.1.0
     */
    protected String connectionName = "";

    /**
     * Liste des tables à préselectionner avant de montrer la dialog.
     * Ceci est utile en cas de reprise d'une sélection précédente.
     * @since 2.1.0
     */
    protected List<SQLTable> preselectedTables = null;

    public ShowTableSelectorAction(JComponent parent, String connectionName) {
        super(parent, new TableSelectorPanel());
        this.connectionName = connectionName;
    }

    /**
     * Permet de changer le titre dans la bannière d'informations du panel
     * de sélection des tables.
     * @param title le titre à afficher.
     * @since 2.1.0
     */
    public void setTitle(String title) {
        TableSelectorPanel panel = (TableSelectorPanel)super.content;
        panel.setTitle(title);
    }

    /**
     * Permet de changer le sous-titre dans la bannière d'informations du panel
     * de sélection des tables.
     * @param subTitle le sous-titre à afficher.
     * @since 2.1.0
     */
    public void setSubTitle(String subTitle) {
        TableSelectorPanel panel = (TableSelectorPanel)super.content;
        panel.setSubTitle(subTitle);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        setDialogTitle("Table selection...");
        dialog.setModal(true);
        TableSelectorPanel panel = (TableSelectorPanel)super.content;
        panel.updateTableList(connectionName);
        panel.setSelectedTablesList(preselectedTables);
        super.actionPerformed(evt);        
    }

    /**
     * Wrapper pour appeler le TableSelectorPanel pour savoir quelles sont les
     * tables quiont été sélectionnées par l'utilisateur.
     * @return List<SQLTable> sélectionnées par l'utilisateur.
     * @since 2.1.0
     */
    public List<SQLTable> getSelectedTablesList() {
        TableSelectorPanel panel = (TableSelectorPanel)super.content;
        return panel.getSelectedTablesList();
    }

    /**
     * Permet de préselectionner dans la GUI la liste des tables passé en
     * paramètre. Ceci pour reprendre une sélection passée par exemple.
     * @param preselectedTables liste des tables à sélectionner.
     *@since 2.1.0
     */
    public void setSelectedTablesList(List<SQLTable> preselectedTables) {
        this.preselectedTables = preselectedTables;
    }

    /**
     * @see TableSelectorPanel#getUserClickButton()
     * @since 2.1.0
     */
    public int getUserClickButton() {
        TableSelectorPanel panel = (TableSelectorPanel)super.content;
        return panel.getUserClickButton();
    }
}
