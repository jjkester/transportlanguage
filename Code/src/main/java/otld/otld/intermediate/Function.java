package otld.otld.intermediate;

import java.util.Arrays;
import java.util.Map;

/**
 * Function in the program.
 *
 * A function has a unique identifier. A function can have zero or more arguments and always has a single return value.
 */
public class Function extends Element implements TypedElement {
    /** The unique identifier of this function. */
    private String id;

    /** The types of the arguments. The last argument is always the return type. */
    private Type[] args;

    /** The variables for the function arguments. */
    private Variable[] variables;

    /** The operations that form the body of this function. */
    private OperationSequence body;

    /**
     * @param id The unique identifier for this function.
     * @param args The types of the arguments for this function. At least one argument is required. The last argument is
     *             always the return type.
     */
    public Function(final String id, final Type... args) {
        assert args.length >= 1;
        this.id = id;
        this.args = args;
        this.variables = new Variable[this.args.length - 1];
        this.body = new OperationSequence();

        for (int i = 0; i < this.variables.length; i++) {
            this.variables[i] = new Variable(this.args[i], String.format("arg%d", i));
        }
    }

    /**
     * @return The unique identifier of this function.
     */
    public final String getId() {
        return this.id;
    }

    /**
     * @return The body of this function.
     */
    public final OperationSequence getBody() {
        return this.body;
    }

    /**
     * Validates whether the variables with which this function is called are of the right type, and if the number of
     * arguments is correct.
     *
     * @param input The variables with which this function is called.
     * @return Whether the given variables are valid arguments for this function.
     */
    public final boolean validateInput(final Variable ... input) {
        final Type[] args = this.getArgs();

        if (args.length == input.length) {
            for (int i = 0; i < args.length; i++) {
                if (!args[i].worksWith(input[i].getType())) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * @return The variables for the arguments of this function.
     */
    public final Variable[] getVariables() {
        return this.variables;
    }

    /**
     * @return The arguments for this function.
     */
    public final Type[] getArgs() {
        return Arrays.copyOf(this.args, this.args.length - 1);
    }

    /**
     * @return The return type of this function.
     */
    public final Type getType() {
        return this.args[this.args.length - 1];
    }

    @Override
    public final String toString() {
        return String.format("Function %s", this.getId());
    }
}
