package otld.otld.intermediate;

/**
 * Value assignment.
 *
 * Assignment of a (constant) value to a variable. Type checking is done implicitly by the class signature. It is
 * recommended to create a value assignment using the factory method on a variable instance.
 *
 * @param <T> The Java type of the value of the destination variable.
 */
public class ValueAssignment<T> extends Assignment {
    /** The value to assign. */
    private T value;

    /**
     * @param target The variable to which the value is assigned.
     * @param value The value to assign.
     */
    public ValueAssignment(final Variable<T> target, final T value) {
        super(target);
        this.value = value;
    }

    /**
     * @return The value to assign.
     */
    public final T getValue() {
        return this.value;
    }

    @Override
    public final String toString() {
        return String.format("Assign ( %s ) -> %s", this.getValue(), this.getTarget());
    }
}
