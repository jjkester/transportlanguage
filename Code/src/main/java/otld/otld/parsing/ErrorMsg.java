package otld.otld.parsing;

/**
 * Enum for all common error types
 */
public enum ErrorMsg {
    VARNOTDEFINED ("This variable has not been defined!"),
    VARALREADYDEFINED ("This variable has already been defined!"),
    FACTALREADYDEFINED ("This factory has already been defined!"),
    FACTUNDEFINED ("This factory has not been defined!"),
    TYPEMISMATCH ("These types do not match!"),
    TYPENOTDEFINED ("This type is undefined!"),
    UNKNOWNVALUE ("This value is not recgonized"),
    SYNTAXERROR ("Syntax error on:");

    private String msg;

    ErrorMsg (String msg) {
        this.msg = msg;
    }

    public String getMessage() {
        return this.msg;
    }
}
