package net.hironico.common.swing.ribbon;

import com.formdev.flatlaf.util.UIScale;
import net.hironico.common.swing.image.ImageIconUtils;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public abstract class AbstractRibbonAction extends AbstractAction {

    private static final String BASE_ICON_PATH = "/icons/png_64/";

    public AbstractRibbonAction(String name) {
        this(name, null);
    }

    public AbstractRibbonAction(String name, String iconName) {
        super(name);        
        if (iconName != null) {
            String fullIconName = String.format("%s%s", BASE_ICON_PATH, iconName);
            ImageIcon icon = ImageIconUtils.createImageIcon(fullIconName, "");
            if (icon != null) {
                int scaledLargeIconSize = UIScale.scale(32);
                int scaledSmallIconSize = UIScale.scale(16);
                putValue(AbstractAction.LARGE_ICON_KEY, ImageIconUtils.getScaledImage(icon, scaledLargeIconSize, scaledLargeIconSize));
                putValue(AbstractAction.SMALL_ICON, ImageIconUtils.getScaledImage(icon, scaledSmallIconSize, scaledSmallIconSize));
            }
        }
    }

    public ImageIcon getSmallIcon() {
        return (ImageIcon)getValue(SMALL_ICON);
    }

    public ImageIcon getLargeIcon() {
        return (ImageIcon)getValue(LARGE_ICON_KEY);
    }
}