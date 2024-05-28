package net.hironico.minisql.ui.editor.action;

import net.hironico.common.swing.ribbon.AbstractRibbonAction;
import net.hironico.minisql.parser.PlSqlParserBaseListener;
import net.hironico.minisql.parser.postgresql.*;
import net.hironico.minisql.ui.MainWindow;
import net.hironico.minisql.ui.editor.QueryPanel;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckSQLAction extends AbstractRibbonAction {
    private static final Logger LOGGER = Logger.getLogger(CheckSQLAction.class.getName());

    private QueryPanel queryPanel = null;

    public CheckSQLAction() {
        super("Check SQL", "icons8_eye_checked_64px.png");
    }

    private void validateSQL(String sql) throws IOException {
        LOGGER.info("Starting SQL validation ...");

        // clear the previous message if any
        queryPanel.setStatusMessage("");

        // create a lexer and parser for the SQL grammar
        CharStream charStream = CharStreams.fromString(sql);
        Lexer lexer = new PostgreSQLLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreSQLParser parser = new PostgreSQLParser(tokens);

        final List<String> errors = new ArrayList<String>();

        // create a listener for the parser that can detect syntax errors
        ANTLRErrorListener errorListener = new ANTLRErrorListener() {
            @Override
            public void reportAmbiguity(Parser parser, DFA dfa, int line, int charPosition, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
                LOGGER.severe("Report ambiguity");
            }

            @Override
            public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
                LOGGER.severe("Report attempting full context");
            }

            @Override
            public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
                LOGGER.severe("Report context sensitivity");
            }

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                // display the syntax error in the Swing component
                int errorLine = line - 1; // adjust for 0-based index
                int errorColumn = charPositionInLine - 1; // adjust for 0-based index
                // highlight the error location or position the cursor
                // display an error message or icon

                String errMsg = String.format("Syntax error at line %d : %d", line, charPositionInLine);
                LOGGER.severe(errMsg);
                queryPanel.setStatusMessage(errMsg);
                errors.add(errMsg);
            }
        };
        parser.addErrorListener(errorListener);

        // parse the SQL statement and check for syntax errors

        ParseTree tree = parser.root();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new PlSqlParserBaseListener(), tree);

        if (errors.isEmpty()) {
            queryPanel.setStatusMessage("SQL seems correct.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Component comp = MainWindow.getInstance().getCurrentTabComponent();
        if (!(comp instanceof QueryPanel)) {
            return;
        }

        this.queryPanel = (QueryPanel) comp;
        String sql = queryPanel.getQueryText();

        try {
            this.validateSQL(sql);
        } catch (IOException ioe) {
            String errMsg = String.format("Cannot check SQL syntax: %s", ioe.getMessage());
            LOGGER.log(Level.SEVERE, errMsg, ioe);
            JOptionPane.showMessageDialog(queryPanel, errMsg);
        }
    }
}
