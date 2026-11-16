package net.hironico.minisql.ui.dataexport;

/**
 * Output formats supported by the data export tool.
 */
public enum ExportFormat {
    CSV("CSV", "csv"),
    JSON("JSON", "json"),
    SQL("SQL INSERT", "sql");

    private final String label;
    private final String extension;

    ExportFormat(String label, String extension) {
        this.label = label;
        this.extension = extension;
    }

    /** @return the default file extension, without the dot */
    public String getExtension() {
        return extension;
    }

    @Override
    public String toString() {
        return label;
    }
}
