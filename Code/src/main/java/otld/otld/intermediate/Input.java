package otld.otld.intermediate;

/**
 * Operation to get input from a user. Only accepts input in the correct type for the variable.
 */
public class Input<T> extends Operation {
    /** The destination variable for the input. */
    private Variable<T> destination;

    /** The query message to display. */
    private String query;

    /**
     * @param query The query message to display.
     * @param destination The destination variable for the input.
     */
    public Input(final String query, final Variable<T> destination) {
        this.query = query;
        this.destination = destination;
    }

    /**
     * @return The query message to display.
     */
    public final String getQuery() {
        return this.query;
    }

    /**
     * @return The destination variable for the input.
     */
    public final Variable<T> getDestination() {
        return this.destination;
    }
}
