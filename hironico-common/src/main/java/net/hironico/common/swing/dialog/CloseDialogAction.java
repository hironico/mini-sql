package net.hironico.common.swing.dialog;

import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Classe utilitaire qui permet de fermer une dialog à partir du contentPane de cette
 * dialog. C'est utile pour ne pas avoir à trainer les références à des JDialog dans tous les
 * sens dans les Panel. On va simplement utiliser SwingUtilities.getWindowAncestor() et ensuite
 * invoker setVisible(false) et puis dispose pour tout fermer proprement.
 * @author hironico
 * @since 2.0.0
 */
public class CloseDialogAction extends AbstractAction {
    protected JComponent contentPane = null;

    public CloseDialogAction(JComponent contentPane) {
        this.contentPane = contentPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (contentPane == null) {
            return;
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(contentPane);

        // tester si la parent window a un conteneur ... sinon ca veut dire qu'on est sur la
        // fenétre principale ... pas cool sinon !
        if (parentWindow.getParent() != null) {
            parentWindow.setVisible(false);
            parentWindow.dispose();
        }
    }

}
