package net.hironico.common.swing.image;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * Cette classe permet de passer des objets images depuis et vers le clipboard
 * système. C'est très pratique pour les copier/coller inter applications.
 * Par exemple, depuis le VisualDb de db tool vers word.
 * @author hironico
 * @since 2.1.0
 */
public class TransferableImage implements Transferable {

    private final Image image;

    public TransferableImage(Image image) {
        this.image = image;
    }

    /**
     * Permet de connaitre les flavors supportées par cet objet transferable.
     * @return DataFlavor.imageFlavor
     * @since 2.1.0
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    /**
     * Permet de savoir si on peut transferer ces données avec ce transferable.
     * @param flavor le type de données à transférer.
     * @return true si le flavor est DataFlavor.imageFlavor
     * @since 2.1.0
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    /**
     * Permet d'obtenir les données proprement dites.
     * @param flavor le type de données demandé. Déclenche unsupported exception si ce n'est pas DataFlavor.imageFlavor
     * @return l'objet image
     * @throws UnsupportedFlavorException si pas de type image flavor.
     * @since 2.1.0
     */
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }
}
