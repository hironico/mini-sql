package net.hironico.minisql.ui.visualdb;

import java.awt.Image;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.*;

import net.hironico.common.swing.image.ImageIconUtils;
import net.hironico.minisql.model.*;
import org.netbeans.api.visual.vmd.VMDGraphScene;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Cette classe modélise une scène de visualisation des tables de la base de données.
 * On utilise la visual library de Netbeans pour créer des noeuds (les tables) avec des
 * pins (les colonnes) qui sont reliés via les clés étrangères (les edges). Cette classe
 * permet de manipuler les objets à afficher dans les composants graphiques.
 * @author hironico
 * @since 2.1.0
 */
public class DBGraphScene extends VMDGraphScene {

    private static final Logger LOGGER = Logger.getLogger(DBGraphScene.class.getName());
    /**
     * icones représentant une table pour les noeuds.
     */
    protected ImageIcon iconTable;
    /**
     * Liste des tables qui sont actuellement en cours d'affihage dans la scène.
     * Cette property est mise à jour par la méthode createScene().
     * @see #createScene(java.util.List)
     * @since 2.1.0
     */
    private final List<SQLTable> displayedTableList = new ArrayList<>();

    /**
     * Ce constructeur charge les icones des éléments graphiques de la scène.
     * @since 2.1.0
     */
    public DBGraphScene() {
        String fullIconName = "/icons/png_64/icons8_data_sheet_64px.png";
        iconTable = ImageIconUtils.createImageIcon(fullIconName, "");
        if (iconTable == null) {
            LOGGER.severe(String.format("Cannot load icon : %s", fullIconName));
            return;
        }
        iconTable = ImageIconUtils.getScaledImage(iconTable, 16,16);
    }

    /**
     * Fournit un accès en lecture à la liste des tables qui sont actuellement
     * en cours d'édition dans cette scène.
     * @return List<SQLTable> liste des tables affichées.
     * @since 2.1.0
     */
    public List<SQLTable> getDisplayedTableList() {
        return displayedTableList;
    }

    /**
     * Permet de retirer tous les noeuds de la scène. Cela signifie aussi de retirer
     * tous les liens et tous les colonnes associées aux tables. C'est automatique
     * avec une scène de type VMDGraphScene.
     * @since 2.1.0
     */
    public void cleanUpScene() {
        LOGGER.finer("Cleanup scene...");
        // il faut faire une recopie pour éviter les exceptions de concurrence d'accès
        List<String> myNodes = new ArrayList<>(getNodes());
        for (String nodeName : myNodes) {
            removeNode(nodeName);
        }
        LOGGER.finer("Cleanup scene complete.");
    }

    /**
     * Permet de créer la scène VisualDb à partir d'une liste de SQLTable
     * <b>dont les colonnes ont déjà été récupérées depuis la base de données.</b>
     * @param tableList liste des tables à afficher.
     * @since 2.1.0
     */
    public void createScene(List<SQLTable> tableList) {
        if (getView() == null) {
            createView();
        }

        tableList.forEach(table -> {
            if (!displayedTableList.contains(table)) {
                displayedTableList.add(table);
            }
        });


        Runnable run = () -> {

            displayedTableList.forEach(table -> {
                String nodeId = createNode(this, (int) (Math.random() * 800), (int) (Math.random() * 800), table);
                if (nodeId != null) { // null means already exists
                    HashMap<String, List<Widget>> pinByCategories = new HashMap<>();
                    table.getColumns().forEach(column -> {
                        String pinId = String.format("%s.%s.%s", table.schemaName, table.name, column.name);
                        VMDPinWidget pinWidget = ((VMDPinWidget) this.addPin(nodeId, pinId));

                        String name = String.format("%s : %s", column.name, column.getTypeString());

                        pinWidget.setProperties(name, null);

                        if (column.isPrimaryKey) {
                            List<Widget> pkPinList = pinByCategories.getOrDefault("Primary keys", new ArrayList<>());
                            pkPinList.add(pinWidget);
                            pinByCategories.putIfAbsent("Primary keys", pkPinList);
                        } else {
                            List<Widget> pkPinList = pinByCategories.getOrDefault("Columns", new ArrayList<>());
                            pkPinList.add(pinWidget);
                            pinByCategories.putIfAbsent("Columns", pkPinList);
                        }
                    });

                    VMDNodeWidget nodeWidget = (VMDNodeWidget) findWidget(nodeId);
                    nodeWidget.sortPins(pinByCategories);
                }
            });

            // try or retry to link tables between them
            displayedTableList.forEach(table -> table.getForeignKeys().values().forEach(fkList -> fkList.forEach(fk -> {
                String sourcePinId = String.format("%s.%s.%s", fk.fkSchemaName, fk.fkTableName, fk.fkColumnName);
                String targetPinId = String.format("%s.%s.%s", fk.pkSchemaName, fk.pkTableName, fk.pkColumnName);
                createEdge(this, sourcePinId, targetPinId);
            })));

            revalidate();
            validate();
            repaint();
        };

        SwingUtilities.invokeLater(run);
    }

    private String createNode(VMDGraphScene scene, int x, int y, SQLObject sqlObject) {
        String nodeId = String.format("%s.%s", sqlObject.schemaName, sqlObject.name);

        if (scene.getNodes().contains(nodeId)) {
            LOGGER.warning(String.format("Scene already contains %s", nodeId));
            return null;
        }

        VMDNodeWidget widget = (VMDNodeWidget) scene.addNode(nodeId);
        widget.setPreferredLocation(new Point(x, y));

        Image image = null;
        switch (sqlObject.type) {
            case TABLE:
            case VIEW:
                image = iconTable == null ? null : iconTable.getImage();
                break;
        }

        // TODO change the background color for a view

        widget.setNodeProperties(image, sqlObject.name, sqlObject.type.toString(), null);

        return nodeId;
    }

    private void createEdge(VMDGraphScene scene, String sourcePinID, String targetPinID) {
        String edgeIDStr = String.format("%s<->%s", sourcePinID, targetPinID);

        if (scene.getEdges().contains(edgeIDStr)) {
            LOGGER.warning(String.format("This edge already exists %s", edgeIDStr));
            return;
        }

        if (!scene.getPins().contains(sourcePinID)) {
            LOGGER.warning(String.format("Source pin is not found: %s", sourcePinID));
            return;
        }

        if (!scene.getPins().contains(targetPinID)) {
            LOGGER.warning(String.format("Target pin is not found: %s", targetPinID));
            return;
        }

        LOGGER.info(String.format("Creating edge: %s", edgeIDStr));
        scene.addEdge(edgeIDStr);
        scene.setEdgeSource(edgeIDStr, sourcePinID);
        scene.setEdgeTarget(edgeIDStr, targetPinID);
    }
}

