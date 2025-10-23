package net.hironico.common.swing.ribbon;

import net.hironico.common.swing.JRoundedPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Box.Filler;

public class RibbonTab extends JRoundedPanel {
    private static final Logger LOGGER = Logger.getLogger(RibbonTab.class.getName());

    private String title = "";

    public RibbonTab(String title) {
        super();
        this.setGradientBackground(true);
        this.setBorderColor(Color.LIGHT_GRAY);
        this.title = title;
        initialize();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected void initialize() {
        LOGGER.info("Init ribbon tab : " + this.title);
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 9999;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 2.0d;

        Filler filler = new Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(10, 10));
        add(filler, gbc);
    }

    public void addGroup(RibbonGroup groupPanel) {
        int col = this.getComponentCount();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = 0;
        gbc.weighty = 1.0d;
        gbc.weightx = 0.0d;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;

        this.add(groupPanel, gbc);

        JSeparator vertSep = new JSeparator(JSeparator.VERTICAL);
        gbc.gridx++;
        this.add(vertSep, gbc);
    }

    /**
     * Override this method to adapt the display of the ribbon tab. For instance, one could
     * update the various toggle button and checkboxes depending of the application state.
     * By default, this method does nothing.
     */
    public void updateDisplay() {

    }
}