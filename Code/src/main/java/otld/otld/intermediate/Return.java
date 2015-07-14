package otld.otld.intermediate;

/**
 * A return statement.
 *
 * Return a variable in a function. Void returns are not supported since void functions are not supported.
 */
public class Return extends Operation {
    /** The variable which is returned. */
    private Variable source;

    /**
     * @param source The variable which is returned.
     */
    public Return(final Variable source) {
        this.source = source;
    }

    /**
     * @return The variable which is created.
     */
    public final Variable getSource() {
        return this.source;
    }

    @Override
    public final String toString() {
        return String.format("Return %s", this.getSource());
    }
}
