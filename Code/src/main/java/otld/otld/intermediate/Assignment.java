package otld.otld.intermediate;

/**
 * Base class for assignment operations.
 *
 * Declares a destination variable, which is common for all assignments.
 */
public abstract class Assignment extends Operation {
    /** The variable to which the value is assigned. */
    private Variable destination;

    /**
     * @param destination The variable to which the value is assigned.
     */
    protected Assignment(final Variable destination) {
        this.destination = destination;
    }

    /**
     * @return The variable to which the value is assigned.
     */
    public final Variable getDestination() {
        return this.destination;
    }
}
