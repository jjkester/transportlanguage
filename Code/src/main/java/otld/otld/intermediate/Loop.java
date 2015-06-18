package otld.otld.intermediate;

/**
 * Loop in the program.
 *
 * A loop consists of three parts: the condition, the condition body and the body. The condition is a boolean variable.
 * The condition body is a piece of code which should set the condition and is run before every iteration and before the
 * condition variable is checked.
 */
public class Loop extends Element {
    /** The variable which determines whether to execute the body or continue. */
    private Variable<Boolean> condition;

    /** The elements which determine the value of the condition. */
    private Elements conditionBody;

    /** The body of the loop. */
    private Elements body;

    /**
     * @param condition The variable which determines whether to execute the body or continue. The variable must be of
     *                  the boolean type.
     */
    public Loop(final Variable<Boolean> condition) {
        this.condition = condition;
        this.conditionBody = new Elements();
        this.body = new Elements();
    }

    /**
     * @return The variable which determines whether to execute the body or continue.
     */
    public final Variable<Boolean> getCondition() {
        return this.condition;
    }

    /**
     * @return The elements which determine the value of the condition.
     */
    public final Elements getConditionBody() {
        return this.conditionBody;
    }

    /**
     * @return The body of the loop.
     */
    public final Elements getBody() {
        return this.body;
    }

    @Override
    public final String toString() {
        return String.format("Loop ( %s )", this.getCondition());
    }
}
