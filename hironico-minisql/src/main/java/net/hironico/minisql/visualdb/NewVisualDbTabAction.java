package net.hironico.minisql.visualdb;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.ui.MainWindow;

/**
 * Permet de créer une vue VisualDb et de l'ajouter dans l'arbre des fenêtres
 * de l'outil db tool.
 * @author hironico
 * @since 2.1.0
 */
public class NewVisualDbTabAction extends AbstractRibbonAction {

    protected static final Logger logger = Logger.getLogger(NewVisualDbTabAction.class.getName());

    public NewVisualDbTabAction() {
        super("Visual Db", "icons8_flow_chart_64px.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                VisualDbPanel panel = new VisualDbPanel();
                MainWindow.getInstance().displayCloseableComponent(panel, "Visual Db");
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
}
