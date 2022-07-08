package ch.ubp.pms.minisql.ui;

import ch.ubp.pms.minisql.model.SQLResultSetTableModel;
import ch.ubp.pms.minisql.ui.renderer.ClobTableCellEditor;
import ch.ubp.pms.minisql.ui.renderer.ClobTableCellRenderer;
import ch.ubp.pms.minisql.ui.renderer.RowHighlightRenderer;
import ch.ubp.pms.swing.ribbon.AbstractRibbonAction;
import ch.ubp.pms.swing.table.FilterableTable;
import ch.ubp.pms.utils.json.JSONFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

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