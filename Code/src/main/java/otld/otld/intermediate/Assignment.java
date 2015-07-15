package otld.otld.intermediate;

/**
 * Base class for assignment operations.
 *
 * Declares a target variable, which is common for all assignments.
 */
public abstract class Assignment extends Operation {
    /** The variable to which the value is assigned. */
    private Variable target;

    /**
     * @param target The variable to which the value is assigned.
     */
    protected Assignment(final Variable target) {
        this.target = target;
    }

    /**
     * @return The variable to which the value is assigned.
     */
    public final Variable getTarget() {
        return this.target;
    }
}
