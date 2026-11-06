package net.hironico.minisql.ui.dbexplorer.action;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.ctrl.SchemaModelExportCallable;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.ui.MainWindow;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exports the structure of the selected tables and views as a JSON document: schema name, then
 * tables by name, each table having its columns, primary key, indexes and foreign keys.
 * The document is displayed in a new editor tab and can be saved to a JSON file.
 */
public class DbObjectExportStructureAction extends AbstractDbExplorerAction {
    private static final Logger LOGGER = Logger.getLogger(DbObjectExportStructureAction.class.getName());

    /** Last directory used to save a model, shared between exports */
    private static File lastSaveDirectory = null;

    public DbObjectExportStructureAction() {
        super("Export Model", "icons8_export_64.png");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        final DbConfig configToUse = DbConfigFile.getConfig(this.getExplorerPanel().getSelectedConnectionName());
        if (configToUse == null || rejectIfMongoDB(configToUse)) {
            return;
        }

        List<SQLObject> objList = this.getExplorerPanel().getAllSelectedSQLObjects();
        if (objList == null || objList.isEmpty()) {
            return;
        }

        List<SQLObject> exportable = objList.stream().filter(SchemaModelExportCallable::isExportable).toList();
        if (exportable.isEmpty()) {
            JOptionPane.showMessageDialog(MainWindow.getInstance(),
                    "Please select at least one table or view to export.",
                    "Export Model", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // display a tab showing the operation is in progress
        RSyntaxTextArea textArea = new RSyntaxTextArea(String.format("Now loading model for %d objects...", exportable.size()));
        textArea.setEditable(false);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane scrollResults = new RTextScrollPane(textArea);
        scrollResults.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        scrollResults.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollResults.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JButton btnSave = new JButton("Save as JSON...");
        btnSave.setEnabled(false);
        String defaultFileName = getDefaultFileName(configToUse, exportable);
        btnSave.addActionListener(evt -> saveModel(textArea, defaultFileName));

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbar.add(btnSave);

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(toolbar, BorderLayout.NORTH);
        pnl.add(scrollResults, BorderLayout.CENTER);

        MainWindow.getInstance().addNewEditorTab(pnl, String.format("JSON Model: %s", configToUse.name));

        // single task: all the metadata is read with one connection, no nested task on the single thread executor
        MainWindow.executorService.submit(() -> {
            String result;
            boolean success;
            try {
                result = new SchemaModelExportCallable(exportable, configToUse).call();
                success = true;
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error while exporting the model.", ex);
                result = "Error while exporting the model: " + ex.getMessage();
                success = false;
            }

            final String text = result;
            final boolean ok = success;
            SwingUtilities.invokeLater(() -> {
                textArea.setText(text);
                textArea.setCaretPosition(0);
                btnSave.setEnabled(ok);
            });
        });
    }

    /**
     * Proposes a file name like connection_schema_model.json for the save dialog.
     */
    private static String getDefaultFileName(DbConfig config, List<SQLObject> objects) {
        List<String> schemas = objects.stream().map(o -> o.schemaName).distinct().toList();
        String schemaPart = schemas.size() == 1 && schemas.getFirst() != null ? "_" + schemas.getFirst() : "";
        return (config.name + schemaPart + "_model").replaceAll("[^A-Za-z0-9._-]+", "_") + ".json";
    }

    /**
     * Saves the exported model displayed in the text area into a JSON file chosen by the user.
     */
    private void saveModel(RSyntaxTextArea textArea, String defaultFileName) {
        JFileChooser chooser = new JFileChooser(lastSaveDirectory);
        chooser.setDialogTitle("Save the model as JSON");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files (*.json)", "json");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(lastSaveDirectory, defaultFileName));

        if (chooser.showSaveDialog(textArea) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().contains(".")) {
            file = new File(file.getParentFile(), file.getName() + ".json");
        }
        if (file.exists()) {
            int answer = JOptionPane.showConfirmDialog(textArea,
                    String.format("The file %s already exists. Overwrite it?", file.getName()),
                    "Save model", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            Files.writeString(file.toPath(), textArea.getText(), StandardCharsets.UTF_8);
            lastSaveDirectory = file.getParentFile();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot save the model.", ex);
            JOptionPane.showMessageDialog(textArea, "Cannot save the model:\n" + ex.getMessage(),
                    "Save model", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public boolean accept(Object sender) {
        return super.accept(sender);
    }
}
