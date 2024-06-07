package net.hironico.minisql.ui.visualdb;

import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @author David Kaspar
 */
class DbGraphNodeBorder implements Border {

    private final Color colorBorder;
    private final Insets insets;
    private final Stroke stroke;
    private final Color color1;
    private final Color color2;

    public DbGraphNodeBorder (Color colorBorder, int thickness, Color color1, Color color2) {
        this.colorBorder = colorBorder;
        this.insets = new Insets (thickness, thickness, thickness, thickness);
        this.stroke = new BasicStroke (thickness);
        this.color1 = color1;
        this.color2 = color2;
    }

    public Insets getInsets () {
        return insets;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        Shape previousClip = gr.getClip ();
        gr.clip (new RoundRectangle2D.Float (bounds.x, bounds.y, bounds.width, bounds.height, 4, 4));

        drawGradient (gr, bounds, color1, color2, 0f, 1f);

        gr.setColor (colorBorder);
        Stroke previousStroke = gr.getStroke ();
        gr.setStroke (stroke);
        gr.draw (new RoundRectangle2D.Float (bounds.x + 0.5f, bounds.y + 0.5f, bounds.width - 1, bounds.height - 1, 4, 4));
        gr.setStroke (previousStroke);

        gr.setClip (previousClip);
    }

    private void drawGradient (Graphics2D gr, Rectangle bounds, Color color1, Color color2, float y1, float y2) {
        y1 = bounds.y + y1 * bounds.height;
        y2 = bounds.y + y2 * bounds.height;
        gr.setPaint (new GradientPaint (bounds.x, y1, color1, bounds.x, y2, color2));
        gr.fill (new Rectangle.Float (bounds.x, y1, bounds.x + bounds.width, y2));
    }

    public boolean isOpaque () {
        return true;
    }

}

