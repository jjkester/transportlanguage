package otld.otld.intermediate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Sequence of elements.
 *
 * An in-order sequence of elements that form a program.
 */
public class Elements extends LinkedList<Element> {
    @Override
    public final String toString() {
        return String.format("Elements < %d >", this.size());
    }
}
