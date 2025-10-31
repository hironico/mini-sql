package net.hironico.common.swing;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class JSplitPaneNoDivider extends JSplitPane {

    private final int dividerDragOffset = 4;
    private boolean drawDividerLine = false;

    public JSplitPaneNoDivider() {
        setDividerSize( 7 );
        setContinuousLayout( true );
    }

    @Override
    public void doLayout() {
        super.doLayout();

        // increase divider width or height
        BasicSplitPaneDivider divider = ((BasicSplitPaneUI)getUI()).getDivider();
        Rectangle bounds = divider.getBounds();
        int dividerDragSize = 9;
        if( orientation == HORIZONTAL_SPLIT ) {
            bounds.x -= dividerDragOffset;
            bounds.width = dividerDragSize;
        } else {
            bounds.y -= dividerDragOffset;
            bounds.height = dividerDragSize;
        }
        divider.setBounds( bounds );
    }

    @Override
    public void updateUI() {
        setUI( new SplitPaneWithZeroSizeDividerUI() );
        revalidate();
    }

    public void setDrawDividerLine(boolean drawDividerLine) {
        this.drawDividerLine = drawDividerLine;
    }

    public boolean isDrawDividerLine() {
        return this.drawDividerLine;
    }

    private class SplitPaneWithZeroSizeDividerUI extends BasicSplitPaneUI {
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new ZeroSizeDivider( this );
        }
    }

    private class ZeroSizeDivider extends BasicSplitPaneDivider {
        public ZeroSizeDivider(BasicSplitPaneUI ui) {
            super( ui );
            super.setBorder( null );
            setBackground( UIManager.getColor( "controlShadow" ) );
        }

        @Override
        public void setBorder( Border border ) {
            // ignore
        }

        @Override
        public void paint( Graphics g ) {
            if(JSplitPaneNoDivider.this.drawDividerLine) {
                g.setColor( getBackground() );
                if( orientation == HORIZONTAL_SPLIT )
                    g.drawLine( dividerDragOffset, 0, dividerDragOffset, getHeight() - 1 );
                else
                    g.drawLine( 0, dividerDragOffset, getWidth() - 1, dividerDragOffset );
            }
        }

        @Override
        protected void dragDividerTo( int location ) {
            super.dragDividerTo( location + dividerDragOffset );
        }

        @Override
        protected void finishDraggingTo( int location ) {
            super.finishDraggingTo( location + dividerDragOffset );
        }
    }
}