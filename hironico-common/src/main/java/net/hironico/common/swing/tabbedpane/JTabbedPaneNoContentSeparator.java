package net.hironico.common.swing.tabbedpane;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class JTabbedPaneNoContentSeparator extends JTabbedPane {

    public JTabbedPaneNoContentSeparator() {
        super();
        initialize();
    }

    /**
     * Sets properties for the look and feel not to display separator with content
     */
    private void initialize() {
        this.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_CONTENT_SEPARATOR, false);
    }

    /**
     * The added component will be inside a container JPAnel with top empty border of 5 pixels
     * @param title the title to be displayed in this tab
     * @param comp the component to be displayed when this tab is clicked
     */
    @Override
    public void addTab(String title, Component comp) {
        WrapperContainer container = new WrapperContainer(comp);
        super.addTab(title, container);
    }

    public Component getWrappedSelectedComponent() {
        Component comp = super.getSelectedComponent();
        if (comp instanceof WrapperContainer container) {
            return container.getWrappedComponent();
        } else {
            return comp;
        }
    }

    public class WrapperContainer extends JPanel {
        private final Component comp;

        public WrapperContainer(Component comp) {
            super();
            this.comp = comp;
            initialize();
        }

        private void initialize() {
            this.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 0, 5, 0);
            gbc.weightx = 1.0d;
            gbc.weighty = 1.0d;
            gbc.fill = GridBagConstraints.BOTH;
            this.add(comp, gbc);
            this.setOpaque(true);
            this.setBackground(JTabbedPaneNoContentSeparator.this.getBackground());
        }

        public Component getWrappedComponent() {
            return comp;
        }
    }
}
