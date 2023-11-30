package net.hironico.minisql.visualdb;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 * Classe de base permettant l'export des scènes de la visual DB vers une image
 * qui seraplacée soit dans le presse papier, soit dans un fichier choisi par
 * l'utilisateur.
 * @author hironico
 * @since 2.1.0
 */
public abstract class ExportSceneImageAction extends AbstractRibbonAction {
    protected static final Logger logger = Logger.getLogger(ExportSceneImageAction.class.getName());

    protected DBGraphScene graphScene;

    public ExportSceneImageAction(String name, String icon, DBGraphScene graphScenetoExport) {
        super(name, icon);
        this.graphScene = graphScenetoExport;
    }

    /**
     * Permet de récupérer une image du graphe actuellement en cours d'affichage
     * @return BufferedImage contenant les données de l'image crée à partir du graphe.
     * @since 2.1.0
     */
    protected BufferedImage getSceneImage() {
        JComponent view = graphScene.getView();
        Dimension dim = view.getSize();
        BufferedImage buffImage = new BufferedImage(dim.width, dim.height,
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = buffImage.createGraphics();
        graphScene.paint(graphics);
        graphics.dispose();
        return buffImage;
    }
}
