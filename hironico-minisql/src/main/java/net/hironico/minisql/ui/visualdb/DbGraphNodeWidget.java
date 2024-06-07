package net.hironico.minisql.ui.visualdb;

import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

public class DbGraphNodeWidget extends VMDNodeWidget {

    public DbGraphNodeWidget(Scene scene) {
        super(scene);
    }

    public DbGraphNodeWidget(Scene scene, VMDColorScheme scheme) {
        super(scene, scheme);
    }

    /**
     * Hack to get the type name widget
     * @return the LabelWidget that holds the node type.
     */
    public Widget getNodeTypeWidget() {
        return super.getHeader().getChildren().get(2);
    }
}
