package otld.otld.intermediate;

import com.google.common.base.Joiner;
import otld.otld.intermediate.exceptions.TypeMismatch;

import java.util.Arrays;

/**
 * A function call.
 *
 * Declares the (custom) function which is called and the arguments. The number of required arguments is defined by the
 * function that is called.
 */
public class Call extends Operation {
    /** The function that is called. */
    private Function function;

    /** The variables that are the arguments of the function. The last variable is always the return target. */
    private Variable[] args;

    /**
     * @param function The function that is called.
     * @param args The variables that are the arguments of the function. The last variable is always the return target.
     * @throws TypeMismatch The types of the argument variables are not compatible with the types of the function.
     */
    public Call(final Function function, final Variable ... args) throws TypeMismatch {
        this.function = function;
        this.args = args;

        if (!function.validateInput(this.getArgs()) || !function.getType().worksWith(this.getVariable().getType())) {
            throw new TypeMismatch();
        }
    }

    /**
     * @return The function that is called.
     */
    public final Function getFunction() {
        return this.function;
    }

    /**
     * @return The variables that are the arguments of the function.
     */
    public final Variable[] getArgs() {
        return Arrays.copyOf(this.args, this.args.length - 1);
    }

    /**
     * @return The variable containing the result of this call.
     */
    public final Variable getVariable() {
        return this.args[this.args.length - 1];
    }

    @Override
    public final String toString() {
        return String.format("Call %s ( %s ) -> %s", this.getFunction(), Joiner.on(", ").join(this.getArgs()), this.getVariable());
    }
}
