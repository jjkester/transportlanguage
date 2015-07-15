package otld.otld.intermediate;

/**
 * A conditional (if/else) block statement.
 *
 * A conditional has two bodies, one for when the condition is true and one for when the condition is false. The
 * condition is a boolean variable.
 */
public class Conditional extends Block {
    /** The variable which determines which body to execute. */
    private Variable<Boolean> condition;

    /** The body for when the condition is true. */
    private OperationSequence bodyTrue;

    /** The body for when the condition is false. */
    private OperationSequence bodyFalse;

    /**
     * @param condition The variable which determines which body to execute. The variable must be of the boolean type.
     */
    public Conditional(final Variable<Boolean> condition) {
        this.condition = condition;
        this.bodyTrue = new OperationSequence();
        this.bodyFalse = new OperationSequence();
    }

    /**
     * @return The variable which determines which body to execute.
     */
    public final Variable<Boolean> getCondition() {
        return this.condition;
    }

    /**
     * @return The body for when the condition is true.
     */
    public final OperationSequence getBodyTrue() {
        return this.bodyTrue;
    }

    /**
     * @return The body for when the condition is false.
     */
    public final OperationSequence getBodyFalse() {
        return this.bodyFalse;
    }

    @Override
    public final String toString() {
        return String.format("Conditional ( %s )", this.getCondition());
    }
}
