package net.hironico.minisql.ui;

import java.awt.event.ActionEvent;

import javax.swing.text.JTextComponent;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;

public class CopyAllAction extends AbstractRibbonAction {
    
    private static final long serialVersionUID = 1L;

    private JTextComponent textComp = null;

    public CopyAllAction(JTextComponent textComp) {
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