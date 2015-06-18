package otld.otld.intermediate;

/**
 * Assignment of a value to a variable in the program.
 *
 * @param <T> The Java type of the value of the destination variable.
 */
public class ValueAssignment<T> extends Assignment {
    /** The value to assign. */
    private T value;

    /**
     * @param destination The variable to which the value is assigned.
     * @param value The value to assign.
     */
    public ValueAssignment(final Variable<T> destination, final T value) {
        super(destination);
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
        return String.format("Assign ( %s ) -> %s", this.getValue(), this.getDestination());
    }
}
