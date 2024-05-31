package net.hironico.minisql.ui.visualdb;

import net.hironico.minisql.DbConfig;
import net.hironico.minisql.DbConfigFile;
import net.hironico.minisql.model.SQLObject;
import net.hironico.minisql.model.SQLTable;
import net.hironico.minisql.ui.MainWindow;
import org.jdesktop.swingx.JXTreeTable;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

/**
 * Use this class to handle drag and drop of SQL Objects inside the application.
 */
public class SQLObjectMoveHandler extends TransferHandler {
    private final Logger LOGGER = Logger.getLogger(SQLObjectMoveHandler.class.getName());

    private final DataFlavor objectArrayFlavor = new DataFlavor(SQLTable[].class, "Array of SQLTable objects");

    private VisualDbPanel dbPanel;

    public static SQLObjectMoveHandler createFor(JXTreeTable treeTable) {
        SQLObjectMoveHandler handler = new SQLObjectMoveHandler();
        treeTable.setTransferHandler(handler);
        return handler;
    }

    /**
     * Create a drop handler for the DB Graph Scene to receive SQLObject to draw.
     * Unsupported objects are refused. See description on the following page :
     * https://netbeans.apache.org/tutorial/main/tutorials/nbm-visual_library/
     * @param dbPanel the DBPanel used to show the DBGraphScene
     * @param scene DB Graph Scene to drow sql objects.
     * @return SQLObjectMoveHandler just created.
     */
    public static SQLObjectMoveHandler createFor(VisualDbPanel dbPanel, DBGraphScene scene) {
        SQLObjectMoveHandler handler = new SQLObjectMoveHandler();
        handler.dbPanel = dbPanel;
        AcceptProvider ap = new AcceptProvider() {
            @Override
            public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
//                Image dragImage = getImageFromTransferable(transferable);
//                JComponent view = scene.getView();
//                Graphics2D g2 = (Graphics2D) view.getGraphics();
//                Rectangle visRect = view.getVisibleRect();
//                view.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
//                g2.drawImage(dragImage,
//                        AffineTransform.getTranslateInstance(point.getLocation().getX(),
//                                point.getLocation().getY()),
//                        null);
                TransferHandler.TransferSupport support = new TransferSupport(scene.getView(), transferable);
                return handler.canImport(support) ? ConnectorState.ACCEPT : ConnectorState.REJECT;
            }

            @Override
            public void accept(Widget widget, Point point, Transferable transferable) {
                TransferHandler.TransferSupport support = new TransferSupport(scene.getView(), transferable);
                handler.importData(support);
            }
        };

        scene.getActions().addAction(ActionFactory.createAcceptAction(ap));
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

                List<SQLObject> objList = MainWindow.getInstance().getSchemaExcplorerPanel().getAllSelectedSQLObjects();
                return objList.toArray(new SQLObject[0]);
            }
        };
    }

}
