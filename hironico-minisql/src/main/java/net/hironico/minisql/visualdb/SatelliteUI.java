package net.hironico.minisql.visualdb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import org.netbeans.api.visual.vmd.VMDGraphScene;
import org.netbeans.api.visual.widget.Scene;

/**
 * Cette classe est utile pour remplacer le SatelliteComponent de la VisualLibrary
 * de netbeans. On va en faire une UI pour le framework JXLayer de telle sorte à
 * décorer par exemple le JScrollPane d'affichage de la scène de la visual library
 * pour affiher le satellite par dessus. L'affichage est conditionné à la porperty
 * satelliteVisible dont le changement déclenche le repaint.
 * @author hironico
 * @since 2.1.0
 */
public class SatelliteUI extends AbstractLayerUI<JComponent> implements Scene.SceneListener, ComponentListener {

    protected VMDGraphScene graphScene = null;
    protected boolean satelliteVisible = false;
    protected Dimension size = new Dimension(150, 150);
    protected Point location = new Point(0, 0);

    public SatelliteUI(DBGraphScene graphScene) {
        this.graphScene = graphScene;
        addNotify();
    }

    public void addNotify() {
        graphScene.addSceneListener(this);
        JComponent viewComponent = graphScene.getView();
        if (viewComponent == null) {
            viewComponent = graphScene.createView();
        }
        viewComponent.addComponentListener(this);
    }

    public void removeNotify() {
        graphScene.getView().removeComponentListener(this);
        graphScene.removeSceneListener(this);
    }

    public boolean isSatelliteVisible() {
        return satelliteVisible;
    }

    public void setSatellitevisible(boolean satelliteVisible) {
        this.satelliteVisible = satelliteVisible;
        graphScene.repaint();
    }

    @Override
    protected void paintLayer(Graphics2D g, JXLayer<? extends JComponent> layer) {
        super.paintLayer(g, layer);

        if (satelliteVisible) {
            Graphics2D gr = (Graphics2D) g;
            Rectangle bounds = graphScene.getBounds();

            double sx = bounds.width > 0 ? (double) size.width / bounds.width : 0.0;
            double sy = bounds.width > 0 ? (double) size.height / bounds.height : 0.0;
            double scale = Math.min(sx, sy);

            int vw = (int) (scale * bounds.width);
            int vh = (int) (scale * bounds.height);
//                int vx = (size.width - vw) / 2;
//                int vy = (size.height - vh) / 2;


            // ok se placer au bon endroit dans le layser ... en haut à droite
            location.x = layer.getView().getWidth() - size.width - 1;
            location.y = 0;

            gr.setColor(Color.lightGray);
            gr.fillRect(location.x, location.y, location.x + size.width - 1, location.y + size.height - 1);
            
            AffineTransform previousTransform = gr.getTransform();
            gr.translate(location.x, location.y);
            gr.scale(scale, scale);

            graphScene.paint(gr);
            gr.setTransform(previousTransform);

            JComponent component = graphScene.getView();
            double zoomFactor = graphScene.getZoomFactor();
            Rectangle viewRectangle = component != null ? component.getVisibleRect() : null;
            if (viewRectangle != null) {
                Rectangle window = new Rectangle(
                        (int) ((double) viewRectangle.x * scale / zoomFactor),
                        (int) ((double) viewRectangle.y * scale / zoomFactor),
                        (int) ((double) viewRectangle.width * scale / zoomFactor),
                        (int) ((double) viewRectangle.height * scale / zoomFactor));
                window.translate(location.x, location.y);
//            Area area = new Area (new Rectangle (vx, vy, vw, vh));
//            area.subtract (new Area (window));
                gr.setColor(new Color(200, 200, 200, 128));
                gr.fill(window);
                gr.setColor(Color.BLACK);
                gr.drawRect(window.x, window.y, window.width - 1, window.height - 1);
            }

            BorderFactory.createEtchedBorder().paintBorder(layer, g, location.x, location.y, location.x + size.width - 1, location.y + size.height - 1);
        }
    }

    @Override
    public void processMouseEvent(MouseEvent e, JXLayer<? extends JComponent> layer) {
        if (e.getSource() != graphScene.getView()) {
            return;
        }

        if (!satelliteVisible) {
            return;
        }

        if (e.getButton() == MouseEvent.NOBUTTON) {
            return;
        }

        Rectangle visibleRect = graphScene.getView().getVisibleRect();
        int x = e.getX() - visibleRect.x;
        int y = e.getY() - visibleRect.y;

        if ((x >= location.x) && (x <= (location.x + size.width))
                && (y >= location.y) && (y <= (location.y + size.height))) {
            moveVisibleRect(new Point(x, y));
            e.consume(); // pour ne pas interragir avec ce qu'il y a dessous !
        } else {
            setSatellitevisible(false);
        }
    }

    @Override
    public void processMouseMotionEvent(MouseEvent e, JXLayer<? extends JComponent> layer) {
        if (e.getSource() != graphScene.getView()) {
            return;
        }

        if (!satelliteVisible) {
            return;
        }

        Rectangle visibleRect = graphScene.getView().getVisibleRect();
        int x = e.getX() - visibleRect.x;
        int y = e.getY() - visibleRect.y;

        if ((x >= location.x) && (x <= (location.x + size.width))
                && (y >= location.y) && (y <= (location.y + size.height))) {
            if ((e.getModifiers() & MouseEvent.MOUSE_PRESSED) > 0) {
                moveVisibleRect(new Point(x, y));
            }
            e.consume(); // pour ne pas interragir avec ce qu'il y a dessous !
        }
    }

    private void moveVisibleRect(Point center) {
        JComponent component = graphScene.getView();
        if (component == null) {
            return;
        }
        double zoomFactor = graphScene.getZoomFactor();
        Rectangle bounds = graphScene.getBounds();

        double sx = bounds.width > 0 ? (double) size.width / bounds.width : 0.0;
        double sy = bounds.width > 0 ? (double) size.height / bounds.height : 0.0;
        double scale = Math.min(sx, sy);

        int vw = (int) (scale * bounds.width);
        int vh = (int) (scale * bounds.height);
//        int vx = (size.width - vw) / 2;
//        int vy = (size.height - vh) / 2;
        int vx = location.x;
        int vy = location.y;

        int cx = (int) ((double) (center.x - vx) / scale * zoomFactor);
        int cy = (int) ((double) (center.y - vy) / scale * zoomFactor);

        Rectangle visibleRect = component.getVisibleRect();
        visibleRect.x = cx - visibleRect.width / 2;
        visibleRect.y = cy - visibleRect.height / 2;
        component.scrollRectToVisible(visibleRect);
    }

    @Override
    public void sceneRepaint() {
        setDirty(true);
    }

    @Override
    public void sceneValidating() {
    }

    @Override
    public void sceneValidated() {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        setDirty(true);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        setDirty(true);
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
}
