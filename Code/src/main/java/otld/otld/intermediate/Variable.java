package otld.otld.intermediate;

import com.google.common.base.Optional;
import otld.otld.intermediate.exceptions.TypeMismatch;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * A variable in the program.
 *
 * A variable has a type parameter which indicates the Java class to use for values.
 *
 * A variable has a required identifier and type. A variable might have an initial value, which is saved here.
 * Computation and storage of the actual value is left to the target language.
 *
 * @param <T> The Java type of the values for this variable.
 */
public class Variable<T> extends Element implements TypedElement {
    /** The unique identifier of this variable. */
    private String id;

    /** The type of this variable. */
    private Type type;

    /** The optional initial value of this variable. */
    private Optional<T> initialValue;

    /**
     * @param type The type of this variable.
     * @param id The unique identifier of this variable.
     * @param initialValue The initial value of this variable.
     */
    protected Variable(final Type type, final String id, final T initialValue) {
        this.type = type;
        this.id = id;
        this.initialValue = Optional.fromNullable(initialValue);
    }

    /**
     * @param type The type of this variable.
     * @param id The unique identifier of this variable.
     */
    public Variable(final Type type, final String id) {
        this(type, id, null);
    }

    /**
     * @return The unique identifier of this variable.
     */
    public final String getId() {
        return this.id;
    }

    /**
     * @return The type of this variable.
     */
    public final Type getType() {
        return this.type;
    }

    /**
     * @return The initial value of this variable. Can be {@code null}.
     */
    public final T getInitialValue() {
        return this.initialValue.orNull();
    }

    /**
     * Creates a new VariableAssignment with this variable as destination.
     *
     * @param source The source variable.
     * @return The assignment.
     * @throws TypeMismatch The types of this and the source variable do not match.
     */
    public final VariableAssignment createVariableAssignment(final Variable<T> source) throws TypeMismatch {
        return new VariableAssignment(this, source);
    }

    /**
     * Creates a new ValueAssignment with this variable as destination.
     *
     * @param value The new value.
     * @return The assignment.
     */
    public final ValueAssignment<T> createValueAssignment(final T value) {
        return new ValueAssignment<T>(this, value);
    }

    /**
     * Creates a new Variable instance. Automatically sets the type parameter and parses the string value to the correct
     * type.
     *
     * Requirements for different types:
     * - {@code BOOL}: {@code true} when {@code initialValue} is {@code "true"} otherwise {@code false}
     * - {@code INT}: {@code initialValue} parsed as integer
     * - {@code CHAR}: the first character of {@code initialValue}
     *
     * @param type The type of this variable.
     * @param id The unique identifier of this variable.
     * @param initialValue The initial value of this variable. Can be {@code null}.
     * @return The variable.
     */
    public static Variable create(final Type type, final String id, final String initialValue) {
        Variable var;

        switch (type) {
            case BOOL:
                Boolean bool = initialValue == null ? null : Boolean.parseBoolean(initialValue);
                var = new Variable<Boolean>(type, id, bool);
                break;
            case INT:
                Integer integer = initialValue == null ? null : Integer.parseInt(initialValue);
                var = new Variable<Integer>(type, id, integer);
                break;
            case CHAR:
                Character character = initialValue == null ? null : initialValue.charAt(0);
                var = new Variable<Character>(type, id, character);
                break;
            default:
                throw new NotImplementedException();
        }

        return var;
    }

    @Override
    public final String toString() {
        return String.format("Variable %s", this.getId());
    }
}
