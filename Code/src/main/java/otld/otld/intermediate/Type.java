package otld.otld.intermediate;

/**
 * The available data types.
 *
 * Types are used at to make sure (at compile time) that variables are used correctly. This prevents unexpected
 * behaviour.
 *
 * {@code ANY} should only be used for operators.
 */
public enum Type {
    ANY,
    BOOL,
    INT,
    CHAR,
    BOOLARR,
    INTARR,
    CHARARR;

    @Override
    public final String toString() {
        return String.format("Type %s", this.name());
    }

    public final boolean worksWith(final Type other) {
        return this == other || this == Type.ANY || other == Type.ANY;
    }
}
