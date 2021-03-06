package otld.otld.intermediate;

import otld.otld.intermediate.exceptions.FunctionAlreadyDeclared;
import otld.otld.intermediate.exceptions.VariableAlreadyDeclared;

import java.util.*;

/**
 * Represents a program.
 *
 * A program is responsible for keeping administration on all variables, functions, etc. which are globally defined.
 * (Currently, all variables and functions are global except for function arguments.) The program body will be executed
 * at the start of the program. In Java terms, this is the static main method of the program.
 */
public class Program {
    /** The identifier of the program. */
    private String id;

    /** The variables in this program indexed by identifier. */
    private Map<String, Variable> variables;

    /** The functions in this program indexed by identifier. */
    private Map<String, Function> functions;

    /** The operations that form the program. */
    private OperationSequence body;

    /**
     * @param id The unique identifier of the program.
     */
    public Program(final String id) {
        this.id = id;
        this.variables = new HashMap<String, Variable>();
        this.functions = new HashMap<String, Function>();
        this.body = new OperationSequence();
    }

    /**
     * @return The unique identifier of the program.
     */
    public final String getId() {
        return this.id;
    }

    /**
     * @param id The identifier of the variable.
     * @return The variable or {@code null} if there is no variable with the given identifier.
     */
    public final Variable getVariable(final String id) {
        return this.variables.get(id);
    }

    /**
     * @param var The variable to add.
     * @throws VariableAlreadyDeclared There already exists a variable with the given name.
     */
    public final void addVariable(final Variable var) throws VariableAlreadyDeclared {
        if (this.variables.containsKey(var.getId())) {
            throw new VariableAlreadyDeclared();
        }
        this.variables.put(var.getId(), var);
    }

    /**
     * @param id The identifier of the function.
     * @return The function or {@code null} if there is no function with the given identifier.
     */
    public final Function getFunction(final String id) {
        return this.functions.get(id);
    }

    /**
     * @param function The function to add.
     * @throws FunctionAlreadyDeclared There already exists a function with the given name.
     */
    public final void addFunction(final Function function) throws FunctionAlreadyDeclared {
        if (this.functions.containsKey(function.getId())) {
            throw new FunctionAlreadyDeclared();
        }
        this.functions.put(function.getId(), function);
    }

    /**
     * @return The body of the program.
     */
    public final OperationSequence getBody() {
        return this.body;
    }

    /**
     * @return The variables in this program.
     */
    public Set<Variable> getVariables() {
        return new HashSet<Variable>(this.variables.values());
    }

    public Set<Function> getFunctions() {
        return new HashSet<Function>(this.functions.values());
    }

    @Override
    public final String toString() {
        return String.format("Program %s", this.getId());
    }
}
