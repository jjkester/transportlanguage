package otld.otld.intermediate;

import java.util.Arrays;

/**
 * Basic operator for use in calls.
 */
public enum Operator implements TypedElement {
    ADDITION        ("+", Type.INT, Type.INT, Type.INT),
    SUBTRACTION     ("-", Type.INT, Type.INT, Type.INT),
    MULTIPLICATION  ("*", Type.INT, Type.INT, Type.INT),
    DIVISION        ("/", Type.INT, Type.INT, Type.INT),
    MODULUS         ("%", Type.INT, Type.INT, Type.INT),

    UMINUS  ("-", Type.INT, Type.INT),

    LAND    ("&", Type.INT, Type.INT, Type.INT),
    LOR     ("|", Type.INT, Type.INT, Type.INT),
    LXOR    ("^", Type.INT, Type.INT, Type.INT),

    AND     ("&&", Type.BOOL, Type.BOOL, Type.BOOL),
    OR      ("||", Type.BOOL, Type.BOOL, Type.BOOL),
    NOT     ("!", Type.BOOL, Type.BOOL, Type.BOOL),
    COMPLTE ("<=", Type.INT, Type.INT, Type.BOOL),
    COMPGTE (">=", Type.INT, Type.INT, Type.BOOL),
    COMPLT  ("<", Type.INT, Type.INT, Type.BOOL),
    COMPGT  (">", Type.INT, Type.INT, Type.BOOL),

    EQUALS  ("==", Type.ANY, Type.ANY, Type.BOOL);

    /** Symbol representation of this operator. */
    private final String symbol;

    /** The types of the arguments. The last argument is always the return type. */
    private final Type[] args;

    /**
     * @param symbol Symbol representation of this operator.
     * @param args The types of the arguments. At least two arguments required. The last argument is always the return
     *             type.
     */
    Operator(final String symbol, final Type... args) {
        this.symbol = symbol;
        this.args = args;
    }

    /**
     * @return The symbol representation of this operator.
     */
    public final String getSymbol() {
        return this.symbol;
    }

    /**
     * @return The types of the arguments without the return type.
     */
    public final Type[] getArgs() {
        return Arrays.copyOf(this.args, this.args.length - 1);
    }

    /**
     * @return The return type.
     */
    public final Type getType() {
        return this.args[this.args.length - 1];
    }

    /**
     * Validates whether the variables with which this operator is used are of the right type, and if the number of
     * arguments is correct.
     *
     * @param input The variables with which this operator is used.
     * @return Whether the given variables are valid arguments for this operator.
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

    @Override
    public final String toString() {
        return this.getSymbol();
    }
}
