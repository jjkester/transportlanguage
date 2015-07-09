package otld.otld.parsing;

/**
 * Error class that prints errors to the System.err log
 */
public class Error {

    private int line;
    private int charpos;
    private String message;
    private Character symbol;

    public Error (int line, int charpos, String message) {
        this.line = line;
        this.charpos = charpos;
        this.message = message;
    }

    public Error (int line, int charpos, String message, Character symbol) {
        this(line, charpos, message);
        this.symbol = symbol;
    }

    /**
     * Returns a formatted error message for the given parameters
     */
    private String getError() {

        String print;

        print = String.format(
                "Error at line:%d:%d: %s",
                line,
                charpos,
                message
        );

        if (message.startsWith(String.valueOf(ErrorMsg.SYNTAXERROR))) {
            print = String.format(
                    "Error at line:%d:%d: %s (Symbol %s)",
                    line,
                    charpos,
                    message,
                    symbol
            );
        }

        return print;
    }
}
