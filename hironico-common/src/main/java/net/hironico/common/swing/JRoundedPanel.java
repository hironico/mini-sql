package net.hironico.common.swing;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel with a rounded border and a gradient background.
 * Used in RibbonTab and other Hironico.net components (eg in mini-sql)
 */
public class JRoundedPanel extends JPanel {

    public static final Color LIGHT_BLUE_COLOR = new Color(236, 243, 250);

    private boolean gradientBackground = false;
    private Color borderColor = Color.LIGHT_GRAY;

    /**
     * Overrides the default paint with a rounded border and a gradient fill
     * @param g the <code>Graphics</code> object to use for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();
        if (gradientBackground) {
            Color color1 = Color.WHITE;
            Color color2 = new Color(229, 233, 238);
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, w, h, 15, 15);
        }
        g2d.setPaint(this.borderColor);
        g2d.drawRoundRect(0, 0, w, h, 15, 15);
    }

    public boolean isGradientBackground() {
        return gradientBackground;
    }

    public void setGradientBackground(boolean gradientBackground) {
        this.gradientBackground = gradientBackground;
        this.repaint();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        this.repaint();
    }
}
