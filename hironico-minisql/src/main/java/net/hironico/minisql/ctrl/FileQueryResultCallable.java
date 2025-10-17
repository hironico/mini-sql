package net.hironico.minisql.ctrl;

import net.hironico.minisql.DbConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Same as QueryResultCallable but reads the contents of the given file path
 * to invoke the QueryResultCallable constructor.
 * <br/>
 * Important: the given file contents will be executed in batchMode = true meaning that commit
 * instructions will be invoked between each SQL statements separated by the "statement separator" property
 * in the configuration of the connection.
 */
public class FileQueryResultCallable extends QueryResultCallable {

    /**
     * Loads the contents of the given file path and then pass it to the QueryResultCallable super implementation
     * This constructor throws IOException id it is not possible to read from the given file path.
     * @param filePath the file to load the query(ies) from
     * @param config the DB config to use to run the queries of the given file path
     * @throws IOException in case the file is not readable
     */
    public FileQueryResultCallable(Path filePath, DbConfig config) throws IOException {
        super(Files.readString(filePath), config, true);
    }
}
