package net.hironico.minisql.ui.visualdb;

import net.hironico.common.swing.ribbon.RibbonGroup;
import net.hironico.common.swing.ribbon.RibbonTab;

public class VisualDbRibbonTab extends RibbonTab {
    private RibbonGroup groupView;
    private RibbonGroup groupImage;
    private RibbonGroup groupLayout;

    public VisualDbRibbonTab() {
        super("Visual Db");
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.addGroup(getGroupView());
        this.addGroup(getGroupImage());
        this.addGroup(getGroupLayout());
    }

    private RibbonGroup getGroupView() {
        if (this.groupView == null) {
            groupView = new RibbonGroup("View");
            groupView.addButton(new ToggleMagnifySceneAction(), RibbonGroup.LARGE);
            groupView.addButton(new ShowNavigationSceneAction(), RibbonGroup.LARGE);
            groupView.addButton(new ZoomPlusSceneAction(), RibbonGroup.SMALL);
            groupView.addButton(new ZoomMinusSceneAction(), RibbonGroup.SMALL);
            groupView.addButton(new ZoomOriginalSceneAction(), RibbonGroup.SMALL);
        }

        return this.groupView;
    }

    private RibbonGroup getGroupImage() {
        if (this.groupImage == null) {
            groupImage = new RibbonGroup("Image");
            groupImage.addButton(new ExportSceneImageToClipboardAction(), RibbonGroup.LARGE);
            groupImage.addButton(new ExportSceneImageToFileAction(), RibbonGroup.LARGE);
        }

        return this.groupImage;
    }

    private RibbonGroup getGroupLayout() {
        if (groupLayout == null) {
            groupLayout = new RibbonGroup("Layout");
            groupLayout.addButton(new AutoLayoutSceneAction(), RibbonGroup.LARGE);
        }

        return groupLayout;
    }
}
