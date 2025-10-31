package net.hironico.minisql.ui.batch;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.ctrl.FileQueryResultCallable;
import net.hironico.minisql.model.SQLResultSetTableModel;
import net.hironico.minisql.ui.MainWindow;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data class representing a batch file node.
 * This class encapsulates information about a file in a batch execution, including
 * execution timing, results, and the ability to run the file asynchronously.
 */
class BatchFileNode {
    private static final Logger LOGGER = Logger.getLogger(BatchFileNode.class.getName());

    /** Executor service for batch file execution with a single thread */
    public static final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /** The file represented by this batch node */
    private final File file;
    
    /** The result of the batch execution */
    private String result = "";
    
    /** Timestamp when execution started */
    private long started = 0;
    
    /** Timestamp when execution ended */
    private long ended = 0;

    /**
     * Constructs a new BatchFileNode for the specified file.
     * 
     * @param file the file to be represented by this batch node
     */
    public BatchFileNode(File file) {
        this.file = file;
    }

    /**
     * Gets the name of the file.
     * 
     * @return the file name
     */
    public String getFileName() {
        return file.getName();
    }

    /**
     * Gets the File object represented by this node.
     * 
     * @return the File instance
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the result of the batch execution.
     * 
     * @return the execution result string
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the result of the batch execution.
     * 
     * @param result the result string to set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Gets the timestamp when execution started.
     * 
     * @return the start timestamp in milliseconds
     */
    public long getStarted() {
        return started;
    }

    /**
     * Sets the timestamp when execution started.
     * 
     * @param started the start timestamp in milliseconds
     */
    public void setStarted(long started) {
        this.started = started;
    }

    /**
     * Gets the timestamp when execution ended.
     * 
     * @return the end timestamp in milliseconds
     */
    public long getEnded() {
        return ended;
    }

    /**
     * Sets the timestamp when execution ended.
     * 
     * @param ended the end timestamp in milliseconds
     */
    public void setEnded(long ended) {
        this.ended = ended;
    }

    /**
     * Calculates the duration of the batch execution.
     * 
     * @return the execution duration in milliseconds
     */
    public long getDuration() {
        return ended - started;
    }

    /**
     * Checks if this node represents a directory.
     * 
     * @return true if the file is a directory, false otherwise
     */
    public boolean isDirectory() {
        return file.isDirectory();
    }

    /**
     * Executes the batch file asynchronously using the specified database configuration.
     * Sets the execution status and timing information, and submits the file for execution.
     * 
     * @param dbConfig the database configuration to use for execution
     * @return a Future representing the pending result of the execution, or null if an error occurred
     */
    public Future<List<SQLResultSetTableModel>> run(DbConfig dbConfig) {
        try {
            this.setResult("Executing...");
            this.started = System.currentTimeMillis();
            FileQueryResultCallable callable = new FileQueryResultCallable(this.getFile().toPath(), dbConfig);
            return BatchFileNode.executorService.submit(callable);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("ERROR: while executing file in a batch: %s", this.getFile().toString()), e);
            String strError = String.format("ERROR: while executing file %s in a batch: %s", this.getFileName(), e.getMessage());
            this.setResult(strError);
            this.ended = System.currentTimeMillis();
            return null;
        }
    }
}
