package net.hironico.minisql.ui;

import java.awt.event.ActionEvent;

import javax.swing.text.JTextComponent;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;

/**
 * Action implementation for copying all text content from a JTextComponent to the system clipboard.
 * This action selects all text in the specified text component and copies it to the clipboard
 * when triggered.
 */
public class CopyAllAction extends AbstractRibbonAction {
    
    private static final long serialVersionUID = 1L;

    /** The text component from which all content will be copied */
    private JTextComponent textComp;

    /**
     * Constructs a new CopyAllAction for the specified text component.
     * Sets the action name to "Copy" and uses the copy icon for visual representation.
     * 
     * @param textComp the JTextComponent whose content will be copied when this action is triggered
     */
    public CopyAllAction(JTextComponent textComp) {
        super("Copy", "icons8_copy_64px_2.png");
        this.textComp = textComp;
    }

    /**
     * Executes the copy operation when this action is triggered.
     * Selects all text in the associated text component and copies it to the system clipboard.
     * 
     * @param evt the action event that triggered this copy operation
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (textComp != null) {
            textComp.selectAll();
            textComp.copy();            
        }
	}

}
