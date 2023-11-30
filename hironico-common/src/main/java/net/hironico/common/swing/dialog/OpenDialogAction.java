package net.hironico.common.swing.dialog;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Classe permettant de mettre au point une action swing
 * qui va ouvrir une JDialog modale avec un contenu specifique.
 * il est possible de surcharger certaines méthodes de setup afin
 * de configurer éventuellement le contenu specifique. Il y a une recherche automatique
 * de la fenétre parent lorsqu'on spécifie un JComponent comme parent de cette dialog. 
 * @author hironico
 * @since 2.0.0
 */
public class OpenDialogAction extends AbstractAction {
    protected static final Logger logger = Logger.getLogger(OpenDialogAction.class.getName());

    protected Window parentWindow = null;
    protected JComponent content = null;
    protected JDialog dialog = null;
    protected String dialogTitle = "";

    /**
     * Certains look and feel centrent les dialog sur le composant parent et
     * non par rapport à la fenêtre parent.
     * @param parent le composant parent
     * @param content le contenu à afficher.
     */
    public OpenDialogAction(JComponent parent, JComponent content) {
        if (parent != null)
            setupParentWindow(parent);
        this.content = content;
        setupDialog();
    }

    /**
     * Permet d'affecter la dialog à la window passée en paramètre.
     * @param parentWindow la fenêtre parent de la dialog.
     * @param content le contenu à afficher.
     */
    public OpenDialogAction(Window parentWindow, JComponent content) {
        this.parentWindow = parentWindow;
        this.content = content;
        setupDialog();
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (dialog == null)
            setupDialog();
        else
            dialog.setTitle(dialogTitle); // le remettre s'il a changé!

        if (parentWindow != null) {
            Point screenLoc = parentWindow.getLocationOnScreen();
            this.dialog.setLocation(screenLoc.x + 20, screenLoc.y + 20);
        } else {
			final Toolkit toolkit = Toolkit.getDefaultToolkit();
        	final Dimension screenSize = toolkit.getScreenSize();
        	final int x = (screenSize.width - dialog.getWidth()) / 2;
        	final int y = (screenSize.height - dialog.getHeight()) / 2;
        	dialog.setLocation(x,y);
		}
        dialog.setVisible(true);
    }

    public JDialog getDialog() {
        return dialog;
    }

    protected void setupParentWindow(JComponent parent) {
        if (parent == null) {
            logger.finer("Parent window is null since parent component is null !");
            parentWindow = null;
            return;
        }

        parentWindow = SwingUtilities.getWindowAncestor(parent);

        if (parentWindow != null)
            logger.finer("Parent container window found is of type: " + parentWindow.getClass().getName());
    }

    protected void setupDialog() {
        if (parentWindow == null)
            setupParentWindow(content);

        this.dialog = new JDialog(parentWindow);
        this.dialog.getContentPane().add(content);
        this.dialog.setTitle(dialogTitle);
        this.dialog.setModal(true);
        this.dialog.setSize(500,300);
        this.dialog.setResizable(false);
    }

}
