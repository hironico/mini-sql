package net.hironico.minisql.model.schema;

import net.hironico.common.utils.json.JSONFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Reads the JSON documents written by the "Export Model" action.
 * A document holds one schema (JSON object) or several schemas (JSON array).
 */
public final class SchemaModelReader {

    private SchemaModelReader() {
    }

    /**
     * Loads a model file.
     *
     * @param file the JSON file
     * @return the schema models of the file, never empty
     * @throws IOException if the file cannot be read, is not a model or has an unsupported format version
     */
    public static List<SchemaModel> load(File file) throws IOException {
        return parse(Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    /**
     * Parses a model document.
     *
     * @param json the JSON document
     * @return the schema models of the document, never empty
     * @throws IOException if the text is not a model or has an unsupported format version
     */
    public static List<SchemaModel> parse(String json) throws IOException {
        String text = json == null ? "" : json.strip();
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        if (text.isEmpty()) {
            throw new IOException("The model document is empty.");
        }

        List<SchemaModel> models;
        try {
            models = text.startsWith("[")
                    ? Arrays.asList(JSONFile.parse(text, SchemaModel[].class))
                    : List.of(JSONFile.parse(text, SchemaModel.class));
        } catch (IOException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new IOException("This is not a model exported by MiniSQL: " + cause.getMessage(), ex);
        }

        if (models.isEmpty()) {
            throw new IOException("The model document contains no schema.");
        }
        for (SchemaModel model : models) {
            if (model == null || model.tables() == null) {
                throw new IOException("This is not a model exported by MiniSQL: no 'tables' found.");
            }
            if (model.formatVersion() > SchemaModel.FORMAT_VERSION || model.formatVersion() < 1) {
                throw new IOException(String.format("Unsupported model format version %d, this version of MiniSQL reads version %d.",
                        model.formatVersion(), SchemaModel.FORMAT_VERSION));
            }
        }
        return models;
    }
}
