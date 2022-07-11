package net.hironico.common.swing.text;

import javax.swing.text.*;

/**
 * Non word wrapping editor kit for use in jtextpane styled documents.
 * Solution proposed by StanislavL 
 * See https://community.oracle.com/thread/1390396
*/
public class NoWrapEditorKit extends StyledEditorKit {
    private static final long serialVersionUID = 1L;

    public ViewFactory getViewFactory() {
        return new StyledViewFactory();
    }

    static class StyledViewFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();

            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new MyLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new NoWrapBoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            return new LabelView(elem);
        }
    }

    static class NoWrapBoxView extends BoxView {
        public NoWrapBoxView(Element elem, int axis) {
            super(elem, axis);
        }

        public void layout(int width, int height) {
            super.layout(65536, height);
        }

        public float getMinimumSpan(int axis) {
            return super.getPreferredSpan(axis);
        }
    }

    static class MyLabelView extends LabelView {
        public MyLabelView(Element elem) {
            super(elem);
        }

        public float getPreferredSpan(int axis) {
            if (axis == View.X_AXIS) {
                TabExpander ex = getTabExpander();

                if (ex == null) {
                    //paragraph implements TabExpander
                    ex = (TabExpander) this.getParent().getParent();
                    getTabbedSpan(0, ex);
                }
            }

            return super.getPreferredSpan(axis);
        }
    }
}