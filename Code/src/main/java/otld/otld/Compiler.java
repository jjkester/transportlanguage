package otld.otld;

import otld.otld.intermediate.*;

/**
 * Base compiler class for OTLD compiler back ends.
 *
 * Compilers use the visitor pattern. The methods are designed to be called while looking into the Program tree of the
 * intermediate representation. There are visitor methods for abstract classes, these can be implemented directly or
 * call the correct more specialized visitor function.
 */
public abstract class Compiler {
    /** The program which is being compiled. */
    protected Program program;

    /** Whether the program has been compiled. */
    protected boolean compiled;

    /**
     * @param program The program which should be compiled.
     */
    protected Compiler(final Program program) {
        this.program = program;
        this.compiled = false;
    }

    /**
     * Compiles the program.
     */
    public abstract void compile();

    /**
     * Returns the compiled program as byte array.
     * @return The byte array containing the compiled program.
     */
    public abstract byte[] asByteArray();

    /**
     * Raises an {@code UnsupportedOperationException}. This method should be called if a compiler does not support a
     * certain feature.
     * @param obj The object that cannot be processed.
     */
    protected void unsupported(final Object obj) {
        throw new UnsupportedOperationException(String.format("<%s> is not supported by this compiler.", obj.toString()));
    }

    /**
     * Returns the compiled program as string if possible. A destination language which does not support strings might
     * return the empty string.
     * @return The string representation of the compiled program or the empty string if not supported.
     */
    public String asString() {
        return "";
    }

    /**
     * @return Whether the program has been compiled.
     */
    public final boolean isCompiled() {
        return this.compiled;
    }

    //
    // VISITOR METHODS
    //

    /**
     * Visitor method for an operator application operation.
     * @param application The application instance.
     */
    protected abstract void visitApplication(final Application application);

    /**
     * Visitor method for an assignment operation.
     * @param assignment The assignment instance.
     */
    protected abstract void visitAssignment(final Assignment assignment);

    /**
     * Visitor method for a block operation.
     * @param block The block instance.
     */
    protected abstract void visitBlock(final Block block);

    /**
     * Visitor method for a break operation.
     * @param brake The break instance.
     */
    protected abstract void visitBreak(final Break brake);

    /**
     * Visitor method for a function call operation.
     * @param call The call instance.
     */
    protected abstract void visitCall(final Call call);

    /**
     * Visitor method for a conditional block operation.
     * @param conditional The conditional instance.
     */
    protected abstract void visitConditional(final Conditional conditional);

    /**
     * Visitor method for a function definition.
     * @param function The function instance.
     */
    protected abstract void visitFunction(final Function function);

    /**
     * Visitor method for an input operation.
     * @param input The input instance.
     */
    protected abstract void visitInput(final Input input);

    /**
     * Visitor method for a loop block operation.
     * @param loop The loop instance.
     */
    protected abstract void visitLoop(final Loop loop);

    /**
     * Visitor method for an operation.
     * @param operation The operation instance.
     */
    protected abstract void visitOperation(final Operation operation);

    /**
     * Visitor method for an operation sequence.
     * @param sequence The operation sequence instance.
     */
    protected abstract void visitOperationSequence(final OperationSequence sequence);

    /**
     * Visitor method for an operator.
     * @param operator The operator instance.
     */
    protected abstract void visitOperator(final Operator operator);

    /**
     * Visitor method for an output operation.
     * @param output The output instance.
     */
    protected abstract void visitOutput(final Output output);

    /**
     * Visitor method for a program.
     * @param program The program instance.
     */
    protected abstract void visitProgram(final Program program);

    /**
     * Visitor method for a return operation.
     * @param returm The return instance.
     */
    protected abstract void visitReturn(final Return returm);

    /**
     * Visitor method for a value assignment operation.
     * @param assignment The value assignment instance.
     */
    protected abstract void visitValueAssignment(final ValueAssignment assignment);

    /**
     * Visitor method for a variable definition.
     * @param variable The variable instance.
     */
    protected abstract void visitVariable(final Variable variable);

    /**
     * Visitor method for a variable assignment operation.
     * @param assignment The variable assignment instance.
     */
    protected abstract void visitVariableAssignment(final VariableAssignment assignment);
}
