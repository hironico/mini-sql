package net.hironico.minisql.ui;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;

import java.util.logging.Logger;
import javax.swing.*;

/**
 * Utility class for generating result component for displaying query results.
 * This can be used when executing a query from the QueryPanel or from the DB explorer when generating
 * an object structure query or whatever SQL call that needs to display something.
 * We use the SQLResultSetTableModel to store data to display and the way to display it on screen.
 * As a consequence, a list of SQLResultSetTableModel can display each result differently.
 */
public abstract class AbstractQueryAction extends AbstractRibbonAction {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(AbstractQueryAction.class.getName());

    public AbstractQueryAction() {
        super("Execute", "icons8_play_64px.png");
        putValue(Action.SHORT_DESCRIPTION, "Execute query of currently selected editor.");
    }
}