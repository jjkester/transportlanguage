package otld.otld.parsing;

/**
 * Error class that represents a parsing error
 */
public class Error {

    /** Line number of the error.*/
    private int line;
    /** Position of the character in the line where the error occured.*/
    private int charpos;
    /** Message that indicates what kind of error occurred. See ErrorMsg for common messages.*/
    private String message;
    /** Symbol that caused the syntax error (if such an error occurred)*/
    private String symbol;

    /**
     * Create an error object that is capable of returning a formatted error message
     * based on the provided parameters.
     * @param line
     * @param charpos
     * @param message
     */
    public Error (int line, int charpos, String message) {
        this(line, charpos, message, null);
    }

    /**
     * Specialised constructor for SyntaxErrors, for other errors see the other constructor.
     * @param line
     * @param charpos
     * @param message
     * @param symbol
     */
    public Error (int line, int charpos, String message, String symbol) {
        this.line = line;
        this.charpos = charpos;
        this.message = message;
        this.symbol = symbol;
    }

    /**
     * Returns a formatted error message for this class
     */
    public String getError() {
        StringBuilder out = new StringBuilder();
        out.append(String.format("Error at line:%d:%d: %s", line, charpos, message));

        if (this.symbol != null) {
            out.append(String.format("%s)", symbol));
        }

        return out.toString();
    }
}
