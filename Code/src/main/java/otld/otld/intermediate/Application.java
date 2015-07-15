package otld.otld.intermediate;

import com.google.common.base.Joiner;
import otld.otld.intermediate.exceptions.TypeMismatch;

import java.util.Arrays;

/**
 * Operator application.
 *
 * Operation which applies an operator to a set of variables. The number of required variables is determined by the
 * operator that is used.
 */
public class Application extends Operation {
    /** The operator that is used. */
    private Operator operator;

    /** The variables that are the arguments of the operator. The last variable is always the return target. */
    private Variable[] args;

    /**
     * @param operator The operator that is used.
     * @param args The variables that are the arguments of the operator. The last variable is always the return target.
     * @throws TypeMismatch The types of the argument variables are not compatible with the types of the operator.
     */
    public Application(final Operator operator, final Variable ... args) throws TypeMismatch {
        this.operator = operator;
        this.args = args;

        if (!operator.validateInput(this.getArgs()) || !operator.getType().worksWith(this.getVariable().getType())) {
            throw new TypeMismatch();
        }
    }

    /**
     * @return The operator that is used.
     */
    public final Operator getOperator() {
        return this.operator;
    }

    /**
     * @return The variables that are the arguments of the operator.
     */
    public final Variable[] getArgs() {
        return this.args.length > 0 ? Arrays.copyOf(this.args, this.args.length - 1) : new Variable[0];
    }

    /**
     * @return The variable containing the result of this application.
     */
    public final Variable getVariable() {
        return this.args[this.args.length - 1];
    }

    @Override
    public final String toString() {
        return String.format("Apply %s ( %s ) -> %s", this.getOperator(), Joiner.on(", ").join(this.getArgs()), this.getVariable());
    }
}
