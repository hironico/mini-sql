package ch.ubp.pms.minisql.ui;

import java.awt.event.ActionEvent;

import javax.swing.text.JTextComponent;

import ch.ubp.pms.swing.ribbon.AbstractRibbonAction;

public class CopyAction extends AbstractRibbonAction {
    
    private static final long serialVersionUID = 1L;

    private JTextComponent textComp = null;

    public CopyAction(JTextComponent textComp) {
        super("Copy", "icons8_copy_64px_2.png");
        this.textComp = textComp;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (textComp != null) {
            textComp.selectAll();
            textComp.copy();            
        }
	}

}