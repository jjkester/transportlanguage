package otld.otld.intermediate;

/**
 * The available data types.
 *
 * Types are used at to make sure (at compile time) that variables are used correctly. This prevents unexpected
 * behaviour.
 */
public enum Type {
    BOOL,
    INT,
    CHAR,
    BOOLARR,
    INTARR,
    CHARARR,;

    @Override
    public final String toString() {
        return String.format("Type %s", this.name());
    }
}
