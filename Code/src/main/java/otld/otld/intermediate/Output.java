package otld.otld.intermediate;

/**
 * Operation to output a value.
 *
 * The value from the variable will be outputted directly after the description string.
 */
public class Output extends Operation {
    /** The source variable for the output. */
    private Variable source;

    /** The description of the value. */
    private String description;

    /**
     * @param description The description of the value.
     * @param source The source variable for the output.
     */
    public Output(final String description, final Variable source) {
        this.description = description;
        this.source = source;
    }

    /**
     * @return The description of the value.
     */
    public final String getDescription() {
        return this.description;
    }

    /**
     * @return The source variable for the output.
     */
    public final Variable getSource() {
        return this.source;
    }
}
