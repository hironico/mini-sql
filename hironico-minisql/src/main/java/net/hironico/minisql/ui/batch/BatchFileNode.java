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
 */
class BatchFileNode {
    private static final Logger LOGGER = Logger.getLogger(BatchFileNode.class.getName());

    /**
     * Executor service for the batch
     */
    public static final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private final File file;
    private String result = "";
    private long started = 0;
    private long ended = 0;

    public BatchFileNode(File file) {
        this.file = file;
    }

    public String getFileName() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getStarted() {
        return started;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    public long getEnded() {
        return ended;
    }

    public void setEnded(long ended) {
        this.ended = ended;
    }

    public long getDuration() {
        return ended - started;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

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
