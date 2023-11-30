package net.hironico.minisql.visualdb;

import java.awt.Image;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

import net.hironico.common.swing.ImageIconUtils;
import net.hironico.minisql.model.SQLColumn;
import net.hironico.minisql.model.SQLTable;
import net.hironico.minisql.model.SQLTableForeignKey;
import org.netbeans.api.visual.vmd.VMDGraphScene;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;

/**
 * Cette classe modélise une scène de visualisation des tables de la base de données.
 * On utilise la visual library de Netbeans pour créer des noeuds (les tables) avec des
 * pins (les colonnes) qui sont reliés via les clés étrangères (les edges). Cette classe
 * permet de manipuler les objets à afficher dans les composants graphiques.
 * @author hironico
 * @since 2.1.0
 */
public class DBGraphScene extends VMDGraphScene {

    protected static final Logger logger = Logger.getLogger(DBGraphScene.class.getName());
    /**
     * icones représentant une table pour les noeuds.
     */
    protected ImageIcon iconTable;
    /**
     * Compteur de liaison pour avoir à nom unique pour chaque liaison. 
     * On préfère cette méthode à celle consistant à associer un nom basé sur 
     * les noms de tables liées ce qui est plus compliqué. Remarque, les ID des 
     * nodes (les tables) correspondent au nom de la table elle même.
     */
    private static int edgeID = 1;
    /**
     * Liste des tables qui sont actuellement en cours d'affihage dans la scène.
     * Cette property est mise à jour par la méthode createScene().
     * @see #createScene(java.util.List)
     * @since 2.1.0
     */
    private List<SQLTable> displayedTableList = null;

    /**
     * Ce constructeur charge les icones des éléments graphiques de la scène.
     * @since 2.1.0
     */
    public DBGraphScene() {
        String fullIconName = "/icons/png_64/icons8_data_sheet_64px.png";
        iconTable = ImageIconUtils.createImageIcon(fullIconName, "");
        if (iconTable == null) {
            logger.severe("Cannot load icon : org/hironico/resource/icons/inconexperience/small/shadow/table.png");
        }
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
        logger.finer("Cleanup scene...");
        // il faut faire une recopie pour éviter les exceptions de concurrence d'accès
        List<String> myNodes = new ArrayList<String>();
        myNodes.addAll(getNodes());
        for (String nodeName : myNodes) {
            removeNode(nodeName);
        }
        logger.finer("Cleanup scene complete.");
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

        displayedTableList = tableList;

        for (SQLTable table : tableList) {
            // creation de la 'boite' pour une table
            createNode(this, (int) (Math.random() * 800), (int) (Math.random() * 800), iconTable.getImage(), table.name, "Table", null);

            // rajoute les noms de colonnes dans les attributs.
            List<SQLColumn> columnList = table.getColumns();
            for (SQLColumn column : columnList) {
                String columnName = column.name;
                createPin(this, table.name, table.name + ":" + columnName, iconTable.getImage(), columnName, columnName);
            }
        }

        // ok maintenant qu'on a toutes les boites, il faut les reliers entre
        // elles à partir des infos de clef étrangère.
        for (SQLTable table : tableList) {
            Map<String, List<SQLTableForeignKey>> fkMap = table.getForeignKeys();
            for (List<SQLTableForeignKey> fkList : fkMap.values()) {
                for (SQLTableForeignKey fk : fkList) {
                    String fkTable = fk.fkTableName;
                    String fkColName = fk.fkColumnName;
                    String pkTable = fk.pkTableName;
                    String pkColName = fk.pkColumnName;

                    createEdge(this, fkTable + ":" + fkColName, pkTable + ":" + pkColName);
                }
            }
        }

        this.moveTo(null);
    }

    private static String createNode(VMDGraphScene scene, int x, int y, Image image, String name, String type, java.util.List<Image> glyphs) {
        String nodeID = name;
        VMDNodeWidget widget = (VMDNodeWidget) scene.addNode(nodeID);
        widget.setPreferredLocation(new Point(x, y));
        widget.setNodeProperties(image, name, type, glyphs);
        return nodeID;
    }

    private static void createPin(VMDGraphScene scene, String nodeID, String pinID, Image image, String name, String type) {
        ((VMDPinWidget) scene.addPin(nodeID, pinID)).setProperties(name, null);
    }

    private static void createEdge(VMDGraphScene scene, String sourcePinID, String targetPinID) {
        String edgeIDStr = "edge" + DBGraphScene.edgeID++;
        scene.addEdge(edgeIDStr);
        System.out.println("createEdge " + sourcePinID + "<->" + targetPinID);
        scene.setEdgeSource(edgeIDStr, sourcePinID);
        scene.setEdgeTarget(edgeIDStr, targetPinID);
    }

    private void moveTo(Point point) {
        int index = 0;
        for (String node : getNodes()) {
            getSceneAnimator().animatePreferredLocation(findWidget(node), point != null ? point : new Point(++index * 100, index * 100));
        }
    }
}

