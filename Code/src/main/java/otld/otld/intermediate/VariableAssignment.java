package otld.otld.intermediate;

import otld.otld.intermediate.exceptions.TypeMismatch;

/**
 * Variable assignment
 *
 * Copies the value of the source variable to the destination variable. Type checking is explicit. It is recommended to
 * create a variable assignment using the factory method on a variable instance.
 */
public class VariableAssignment extends Assignment {
    /** The variable which contains the value to assign. */
    private Variable source;

    /**
     * @param destination The variable to which the value is assigned.
     * @param source The variable which contains the value to assign.
     * @throws TypeMismatch The types of the source and destination variables do not match.
     */
    public VariableAssignment(final Variable destination, final Variable source) throws TypeMismatch {
        super(destination);

        if (!destination.getType().equals(source.getType())) {
            throw new TypeMismatch();
        }

        this.source = source;
    }

    /**
     * @return The variable which contains the value to assign.
     */
    public Variable getSource() {
        return this.source;
    }

    @Override
    public final String toString() {
        return String.format("Assign ( %s ) -> %s", this.getSource(), this.getDestination());
    }
}
