package net.hironico.minisql.ui.visualdb;

import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.anchor.PointShapeFactory;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.vmd.*;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.vmd.VMDOriginalColorScheme;
import org.openide.util.ImageUtilities;

import java.awt.*;
import java.util.logging.Logger;

public class DbColorScheme extends VMDColorScheme {
    private static final Logger LOGGER = Logger.getLogger(DbColorScheme.class.getName());

    private Color baseColor;

    private Color COLOR_NORMAL = new Color (0xBACDF0);
    private Color COLOR_HIGHLIGHTED = new Color (0x316AC5);

    //    private static final Color COLOR0 = new Color (169, 197, 235);
    private Color COLOR1 = new Color (221, 235, 246);
    private Color COLOR2 = new Color (255, 255, 255);

    public static final Color COLOR60_SELECT = new Color (0xFF8500);
    private Color COLOR60_HOVER = new Color (0x5B67B0);
    private Color COLOR60_HOVER_BACKGROUND = new Color (0xB0C3E1);

    private Border BORDER60 = new DbGraphNodeBorder(COLOR_NORMAL, 2, COLOR1, COLOR2);
    private Border BORDER60_SELECT = new DbGraphNodeBorder(COLOR60_SELECT, 2, COLOR1, COLOR2);
    private Border BORDER60_HOVER = new DbGraphNodeBorder(COLOR60_HOVER, 2, COLOR1, COLOR2);

    static final Border BORDER_PIN = BorderFactory.createOpaqueBorder (2, 8, 2, 8);
    private static final Border BORDER60_PIN_SELECT = BorderFactory.createCompositeBorder (BorderFactory.createLineBorder (0, 1, 0, 1, COLOR60_SELECT), BorderFactory.createLineBorder (2, 7, 2, 7, COLOR60_SELECT));
//        private static final Border BORDER60_PIN_HOVER = BorderFactory.createLineBorder (2, 8, 2, 8, COLOR60_HOVER);

    private static final PointShape POINT_SHAPE60_IMAGE = PointShapeFactory.createImagePointShape (ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-pin-60.png")); // NOI18N

    private Color BORDER_CATEGORY_BACKGROUND = new Color (0xCDDDF8);

    public DbColorScheme(String baseColorCode) {
        try {
            Color baseColor = Color.decode((baseColorCode));
            this.COLOR_NORMAL = baseColor;
            this.COLOR_HIGHLIGHTED = baseColor;
            this.COLOR1 = baseColor;
            this.COLOR2 = Color.WHITE;
            this.COLOR60_HOVER = baseColor.darker();
            this.COLOR60_HOVER_BACKGROUND = baseColor.darker();
            this.BORDER_CATEGORY_BACKGROUND = baseColor.darker();
            this.BORDER60 = new DbGraphNodeBorder(COLOR_NORMAL, 2, COLOR1, COLOR2);
            this.BORDER60_SELECT = new DbGraphNodeBorder(COLOR60_SELECT, 2, COLOR1, COLOR2);
            this.BORDER60_HOVER = new DbGraphNodeBorder(COLOR60_HOVER, 2, COLOR1, COLOR2);
        } catch (NumberFormatException nfe) {
            LOGGER.severe("Cannot decode base color code. Let color as default !");
        }
    }

    public void installUI (VMDNodeWidget widget) {
        widget.setBorder (BORDER60);

        Widget header = widget.getHeader ();
        header.setBackground (COLOR60_HOVER_BACKGROUND);
        widget.getNodeNameWidget().setForeground(getLabelColorFromBackground(COLOR60_HOVER_BACKGROUND));
        // hack to get the type label widget since the getter is not there in the library
        header.getChildren().get(2).setForeground(getLabelColorFromBackground(COLOR60_HOVER_BACKGROUND));
        header.setBorder (BORDER_PIN);

        Widget pinsSeparator = widget.getPinsSeparator ();
        pinsSeparator.setForeground (BORDER_CATEGORY_BACKGROUND);
    }

    public void updateUI (VMDNodeWidget widget, ObjectState previousState, ObjectState state) {
        if (! previousState.isSelected ()  &&  state.isSelected ())
            widget.bringToFront ();

        boolean hover = state.isHovered () || state.isFocused ();
        widget.getHeader ().setOpaque (hover);

        if (state.isSelected ())
            widget.setBorder (BORDER60_SELECT);
        else if (state.isHovered ())
            widget.setBorder (BORDER60_HOVER);
        else if (state.isFocused ())
            widget.setBorder (BORDER60_HOVER);
        else
            widget.setBorder (BORDER60);
    }

    public void installUI (VMDConnectionWidget widget) {
        widget.setSourceAnchorShape (AnchorShape.NONE);
        widget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        widget.setPaintControlPoints (true);
    }

    public void updateUI (VMDConnectionWidget widget, ObjectState previousState, ObjectState state) {
        if (state.isSelected ())
            widget.setForeground (COLOR60_SELECT);
        else if (state.isHighlighted ())
            widget.setForeground (COLOR_HIGHLIGHTED);
        else if (state.isHovered ()  ||  state.isFocused ())
            widget.setForeground (COLOR60_HOVER);
        else
            widget.setForeground (COLOR_NORMAL);

        if (state.isSelected ()  ||  state.isHovered ()) {
            widget.setControlPointShape (PointShape.SQUARE_FILLED_SMALL);
            widget.setEndPointShape (PointShape.SQUARE_FILLED_BIG);
            widget.setControlPointCutDistance (0);
        } else {
            widget.setControlPointShape (PointShape.NONE);
            widget.setEndPointShape (POINT_SHAPE60_IMAGE);
            widget.setControlPointCutDistance (5);
        }
    }

    public void installUI (VMDPinWidget widget) {
        widget.setBorder (BORDER_PIN);
        widget.setBackground (COLOR60_HOVER_BACKGROUND);

        // todo get the actual background below the pin widget which is not opaque
        // the background is a gradient then we must pick the color below the pin widget
        // widget.getPinNameWidget().setForeground(getLabelColorFromBackground(COLOR60_HOVER_BACKGROUND));
    }

    public void updateUI (VMDPinWidget widget, ObjectState previousState, ObjectState state) {
        widget.setOpaque (state.isHovered ()  ||  state.isFocused ());
        if (state.isSelected ())
            widget.setBorder (BORDER60_PIN_SELECT);
        else
            widget.setBorder (BORDER_PIN);
    }

    public int getNodeAnchorGap (VMDNodeAnchor anchor) {
        return 4;
    }

    public boolean isNodeMinimizeButtonOnRight (VMDNodeWidget widget) {
        return true;
    }

    public Image getMinimizeWidgetImage (VMDNodeWidget widget) {
        return widget.isMinimized ()
                ? ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-expand-60.png") // NOI18N
                : ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-collapse-60.png"); // NOI18N
    }

    public Widget createPinCategoryWidget (VMDNodeWidget widget, String categoryDisplayName) {
        return createPinCategoryWidgetCore (widget, categoryDisplayName, false);
    }

    private Widget createPinCategoryWidgetCore (VMDNodeWidget widget, String categoryDisplayName, boolean changeFont) {
        Scene scene = widget.getScene ();
        LabelWidget label = new LabelWidget (scene, categoryDisplayName);
        label.setOpaque (true);
        label.setBackground (BORDER_CATEGORY_BACKGROUND);
        label.setForeground (getLabelColorFromBackground(BORDER_CATEGORY_BACKGROUND));
        if (changeFont) {
            Font fontPinCategory = scene.getDefaultFont ().deriveFont (10.0f);
            label.setFont (fontPinCategory);
        }
        label.setAlignment (LabelWidget.Alignment.CENTER);
        label.setCheckClipping (true);
        return label;
    }

    private Color getLabelColorFromBackground(Color bg) {
        if ((bg.getRed()*0.299 + bg.getGreen()*0.587 + bg.getBlue()*0.114) > 186) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }
}