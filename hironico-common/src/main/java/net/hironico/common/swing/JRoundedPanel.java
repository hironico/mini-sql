package net.hironico.common.swing;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel with a rounded border and optional gradient background.
 * This specialized panel provides visual enhancement for UI components with rounded corners
 * and customizable styling options. Commonly used in RibbonTab and other 
 * Hironico.net components for a modern, polished appearance.
 */
public class JRoundedPanel extends JPanel {

    /** Default light blue color used for gradient backgrounds */
    public static final Color LIGHT_BLUE_COLOR = new Color(236, 243, 250);

    /** Flag indicating whether gradient background is enabled */
    private boolean gradientBackground = false;
    
    /** Color used for drawing the rounded border */
    private Color borderColor = Color.LIGHT_GRAY;

    /**
     * Overrides the default paint with a rounded border and optional gradient fill.
     * Provides custom rendering with rounded corners and gradient background when enabled.
     * 
     * @param g the Graphics object to use for painting
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

    /**
     * Checks if gradient background is currently enabled.
     * 
     * @return true if gradient background is enabled, false otherwise
     */
    public boolean isGradientBackground() {
        return gradientBackground;
    }

    /**
     * Enables or disables gradient background rendering.
     * When enabled, the panel displays a gradient fill from white to light blue.
     * 
     * @param gradientBackground true to enable gradient background, false to disable
     */
    public void setGradientBackground(boolean gradientBackground) {
        this.gradientBackground = gradientBackground;
        this.repaint();
    }

    /**
     * Gets the current border color.
     * 
     * @return the Color used for drawing the rounded border
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the border color for the rounded rectangle.
     * Changes the color used to draw the panel's rounded border.
     * 
     * @param borderColor the Color to use for the border
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        this.repaint();
    }
}
