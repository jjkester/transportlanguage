package otld.otld.intermediate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * List of operations.
 *
 * An in-order sequence of elements that form a program or method body.
 */
public class OperationSequence extends LinkedList<Operation> {
    @Override
    public final String toString() {
        return String.format("OperationSequence < %d >", this.size());
    }
}
