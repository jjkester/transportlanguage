package otld.otld.intermediate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Sequence of elements.
 *
 * An in-order sequence of elements that form a program.
 */
public class OperationSequence extends LinkedList<Operation> {
    @Override
    public final String toString() {
        return String.format("OperationSequence < %d >", this.size());
    }
}
