package otld.otld.intermediate;

/**
 * Conditional statement in the program.
 *
 * A conditional has two bodies, one for when the condition is true and one for when the condition is false. The
 * condition is a boolean variable.
 */
public class Conditional extends Element {
    /** The variable which determines which body to execute. */
    private Variable<Boolean> condition;

    /** The body for when the condition is true. */
    private Elements bodyTrue;

    /** The body for when the condition is false. */
    private Elements bodyFalse;

    /**
     * @param condition The variable which determines which body to execute. The variable must be of the boolean type.
     */
    public Conditional(final Variable<Boolean> condition) {
        this.condition = condition;
        this.bodyTrue = new Elements();
        this.bodyFalse = new Elements();
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
    public final Elements getBodyTrue() {
        return this.bodyTrue;
    }

    /**
     * @return The body for when the condition is false.
     */
    public final Elements getBodyFalse() {
        return this.bodyFalse;
    }
}
