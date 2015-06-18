package otld.otld.intermediate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Sequence of elements.
 *
 * An in-order sequence of elements that form a program.
 */
public class Elements {
    /** List of the elements. */
    private List<Element> elements;

    public Elements() {
        this.elements = new LinkedList<Element>();
    }

    /**
     * @param element The element to append.
     */
    public final void append(final Element element) {
        this.elements.add(element);
    }

    /**
     * @param i The sequence number of the element.
     * @return The element on the given position.
     */
    public final Element get(final int i) {
        return this.elements.get(i);
    }

    /**
     * @return Iterator for the elements.
     */
    public final Iterator<Element> iterator() {
        return this.elements.iterator();
    }
}
