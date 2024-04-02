package net.hironico.common.swing.ribbon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel {
    public RoundedPanel() {
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics = (Graphics2D)g.create();
        int width = getWidth();
        int height = getHeight();
        graphics.setPaint(Color.LIGHT_GRAY);
        graphics.drawRoundRect(0, 0, width, height, 15, 15);
    }
}
