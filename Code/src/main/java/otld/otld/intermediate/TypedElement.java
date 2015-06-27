package otld.otld.intermediate;

/**
 * Interface for Element subclasses that are typed.
 *
 * This interface standardizes the way of querying the (return) type of an element.
 */
public interface TypedElement {
    /**
     * @return The (return) type of this element.
     */
    Type getType();
}
