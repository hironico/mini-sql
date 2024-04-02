package net.hironico.minisql.ui.visualdb;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLTable;
import net.hironico.minisql.ui.MainWindow;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLObjectMoveHandler extends TransferHandler {
    private final Logger LOGGER = Logger.getLogger(SQLObjectMoveHandler.class.getName());

    private final DataFlavor objectArrayFlavor = new DataFlavor(SQLTable[].class, "Array of SQLTable objects");

    private VisualDbPanel dbPanel;

    public static SQLObjectMoveHandler createFor(JXTreeTable treeTable) {
        SQLObjectMoveHandler handler = new SQLObjectMoveHandler();
        treeTable.setTransferHandler(handler);
        return handler;
    }

    public static SQLObjectMoveHandler createFor(VisualDbPanel dbPanel, JComponent sceneView) {
        SQLObjectMoveHandler handler = new SQLObjectMoveHandler();
        dbPanel.setTransferHandler(handler);
        sceneView.setTransferHandler(handler);
        handler.dbPanel = dbPanel;
        return handler;
    }

    @Override
    public boolean canImport(TransferSupport info) {
        return info.isDataFlavorSupported(objectArrayFlavor);
    }

    @Override
    public boolean importData(TransferSupport transferSupport) {
        Transferable t = transferSupport.getTransferable();

        boolean success = false;
        try {
            String dbConfigName = MainWindow.getInstance().getSchemaExcplorerPanel().getSelectedConnectionName();
            DbConfig dbConfig = DbConfigFile.getConfig(dbConfigName);

            SQLObject[] importedData = (SQLObject[])t.getTransferData(objectArrayFlavor);
            dbPanel.addSQLObjects(Arrays.asList(importedData), dbConfig);
            success = true;
        } catch (UnsupportedFlavorException | IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot drag n drop SQL Tables to VisualDbPanel", e);
        }
        return success;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    public Transferable createTransferable(JComponent source) {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                DataFlavor[] df = new DataFlavor[1];
                df[0] = objectArrayFlavor;
                return df;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return true;
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                SQLObject obj = MainWindow.getInstance().getSchemaExcplorerPanel().getSelectedSQLObject();
                SQLObject[] valuesToTransfer = new SQLObject[1];
                valuesToTransfer[0] = obj;
                return valuesToTransfer;
            }
        };
    }

}
