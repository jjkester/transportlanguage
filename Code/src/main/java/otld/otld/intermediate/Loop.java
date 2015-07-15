package otld.otld.intermediate;

/**
 * Loop (while) block statement.
 *
 * A loop consists of three parts: the condition, the condition body and the body. The condition is a boolean variable.
 * The condition body is a piece of code which should set the condition and is run before every iteration and before the
 * condition variable is checked.
 */
public class Loop extends Block {
    /** The variable which determines whether to execute the body or continue. */
    private Variable<Boolean> condition;

    /** The operations which determine the value of the condition. */
    private OperationSequence conditionBody;

    /** The body of the loop. */
    private OperationSequence body;

    /**
     * @param condition The variable which determines whether to execute the body or continue. The variable must be of
     *                  the boolean type.
     */
    public Loop(final Variable<Boolean> condition) {
        this.condition = condition;
        this.conditionBody = new OperationSequence();
        this.body = new OperationSequence();
    }

    /**
     * @return The variable which determines whether to execute the body or continue.
     */
    public final Variable<Boolean> getCondition() {
        return this.condition;
    }

    /**
     * @return The operations which determine the value of the condition.
     */
    public final OperationSequence getConditionBody() {
        return this.conditionBody;
    }

    /**
     * @return The body of the loop.
     */
    public final OperationSequence getBody() {
        return this.body;
    }

    @Override
    public final String toString() {
        return String.format("Loop ( %s )", this.getCondition());
    }
}
