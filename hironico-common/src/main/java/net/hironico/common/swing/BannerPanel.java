package net.hironico.common.swing;

import org.jdesktop.swingx.VerticalLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.*;

public class BannerPanel extends JPanel {
    private String title;
    private String description;

    private Color primaryColor;
    private Color secondaryColor;
    private Color titleColor = Color.BLACK;
    private Color descriptionColor = Color.BLACK;

    private JLabel lblTitle;
    private JTextPane lblDescription;

    public BannerPanel() {
        this("Title", "Description");
    }

    public BannerPanel(String title, String description) {
        this(title, description, Color.LIGHT_GRAY, Color.WHITE);
    }

    public BannerPanel(String title, String description, Color primaryColor, Color secondaryColor) {
        this.title = title;
        this.description = description;
        this.setPrimaryColor(primaryColor);
        this.setSecondaryColor(secondaryColor);

        initialize();
    }

    private void initialize() {
        this.setLayout(new VerticalLayout());
        this.add(getLblTitle());
        this.add(getLblDescription());
    }

    private JLabel getLblTitle() {
        if (lblTitle == null) {
            lblTitle = new JLabel(getTitle());
            Font font = lblTitle.getFont();
            lblTitle.setFont(new Font(font.getName(), font.getStyle(), 25));
        }

        return lblTitle;
    }

    private JTextPane getLblDescription() {
        if (lblDescription == null) {
            this.lblDescription = new JTextPane();
            lblDescription.setText(this.getDescription());
            lblDescription.setOpaque(false);
            Font font = lblTitle.getFont();
            lblTitle.setFont(new Font(font.getName(), Font.PLAIN, font.getSize()));
        }

        return lblDescription;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, this.getPrimaryColor(), width, height, this.getSecondaryColor());
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 100);
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(Color primaryColor) {
        this.primaryColor = primaryColor;
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(Color secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public Color getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(Color titleColor) {
        this.titleColor = titleColor;
    }

    public Color getDescriptionColor() {
        return descriptionColor;
    }

    public void setDescriptionColor(Color descriptionColor) {
        this.descriptionColor = descriptionColor;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
        this.getLblTitle().setText(title);
    }

    public void setDescription(String description) {
        this.description = description;
        this.getLblDescription().setText(description);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Gradient Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BannerPanel panel = new BannerPanel("Title", "Description");
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
