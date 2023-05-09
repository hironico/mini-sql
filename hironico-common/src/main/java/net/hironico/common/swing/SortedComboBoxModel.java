package net.hironico.common.swing;

import java.util.*;
import javax.swing.*;

/**
 *  Custom model to make sure the items are stored in a sorted order.
 *  The default is to sort in the natural order of the item, but a
 *  Comparator can be used to customize the sort order.
 */
public class SortedComboBoxModel<E extends Comparable<E>> extends DefaultComboBoxModel<E> {
    private final Comparator<E> comparator;

    /*
     *  Create an empty model that will use the natural sort order of the item
     */
    public SortedComboBoxModel() {
        this(null);
    }

    /*
     *  Create an empty model that will use the specified Comparator
     */
    public SortedComboBoxModel(Comparator<E> comparator) {
        super();
        this.comparator = comparator;
    }

    @Override
    public void addElement(E element) {
        insertElementAt(element, 0);
    }

    @Override
    public void insertElementAt(E element, int index) {
        int size = getSize();

        //  Determine where to insert element to keep model in sorted order

        for (index = 0; index < size; index++) {
            if (comparator != null) {
                E o = getElementAt(index);

                if (comparator.compare(o, element) > 0)
                    break;
            } else {
                Comparable<E> c = getElementAt(index);

                if (c.compareTo(element) > 0)
                    break;
            }
        }

        super.insertElementAt(element, index);

        //  Select an element when it is added to the beginning of the model

        if (index == 0 && element != null) {
            setSelectedItem(element);
        }
    }
}