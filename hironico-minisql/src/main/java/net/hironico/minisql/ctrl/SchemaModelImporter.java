package net.hironico.minisql.ctrl;

import net.hironico.minisql.model.schema.SchemaDdlGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes the DDL statements generated from an exported model on a target connection.
 * Each statement is executed (and committed) on its own so that the tables already created are kept
 * if a later statement fails. This class has no dependency on Swing so that it can be unit tested.
 */
public class SchemaModelImporter {

    /** Callback used to report the progress. Called from the import thread. */
    public interface ImportListener {
        /**
         * Called after each statement.
         *
         * @param done      number of statements executed so far (successful or not)
         * @param total     number of statements to execute
         * @param statement the statement just executed
         * @param error     the error, null if the statement succeeded
         */
        void statementDone(int done, int total, SchemaDdlGenerator.Statement statement, SQLException error);
    }

    /**
     * A failed statement.
     *
     * @param statement the statement
     * @param message   the database error message
     */
    public record Failure(SchemaDdlGenerator.Statement statement, String message) {
    }

    /**
     * Summary of the import.
     *
     * @param executed  number of successful statements
     * @param failures  failed statements, DROP errors of missing tables included
     * @param cancelled true if the import was cancelled by the user
     * @param stopped   true if the import stopped on the first error
     */
    public record ImportResult(int executed, List<Failure> failures, boolean cancelled, boolean stopped) {
        /** @return true if there is a failure other than dropping a table that does not exist */
        public boolean hasErrors() {
            return failures.stream().anyMatch(f -> f.statement().phase() != SchemaDdlGenerator.Phase.DROP);
        }
    }

    private final List<SchemaDdlGenerator.Statement> statements;
    private boolean continueOnError = false;
    private volatile boolean cancelRequested = false;

    /**
     * Creates an importer for the given statements.
     *
     * @param statements statements in execution order
     */
    public SchemaModelImporter(List<SchemaDdlGenerator.Statement> statements) {
        this.statements = statements;
    }

    /**
     * Defines the behaviour on errors. DROP errors (table does not exist) never stop the import.
     *
     * @param continueOnError true to execute the remaining statements after an error
     */
    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    /** Requests the running import to stop after the current statement. */
    public void cancel() {
        this.cancelRequested = true;
    }

    /**
     * Executes the statements on the given connection. The connection is not closed.
     *
     * @param conn     open connection to the target database
     * @param listener progress listener, may be null
     * @return the import summary
     * @throws SQLException if the connection is unusable
     */
    public ImportResult run(Connection conn, ImportListener listener) throws SQLException {
        cancelRequested = false;
        List<Failure> failures = new ArrayList<>();
        int executed = 0;
        int done = 0;
        boolean stopped = false;
        boolean autoCommit = conn.getAutoCommit();
        int total = statements.size();

        for (SchemaDdlGenerator.Statement st : statements) {
            if (cancelRequested) {
                break;
            }
            SQLException error = null;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(st.sql());
                if (!autoCommit) {
                    conn.commit();
                }
                executed++;
            } catch (SQLException ex) {
                error = ex;
                failures.add(new Failure(st, ex.getMessage()));
                if (!autoCommit) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        // nothing more to do
                    }
                }
            }
            done++;
            if (listener != null) {
                listener.statementDone(done, total, st, error);
            }
            if (error != null && !continueOnError && st.phase() != SchemaDdlGenerator.Phase.DROP) {
                stopped = true;
                break;
            }
        }

        return new ImportResult(executed, failures, cancelRequested, stopped);
    }
}
