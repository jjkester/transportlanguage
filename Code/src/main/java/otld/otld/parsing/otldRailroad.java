package otld.otld.parsing;

import javafx.collections.transformation.SortedList;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import otld.otld.grammar.otldBaseListener;
import otld.otld.grammar.otldLexer;
import otld.otld.grammar.otldParser;
import otld.otld.intermediate.*;
import otld.otld.intermediate.exceptions.FunctionAlreadyDeclared;
import otld.otld.intermediate.exceptions.TypeMismatch;
import otld.otld.intermediate.exceptions.VariableAlreadyDeclared;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

/** Base visitor for the OTLD intermediate representation. */
public class otldRailroad extends otldBaseListener {

    /** The program that is being parsed by this visitor. */
    private Program city;
    /** All of the functions encountered during parsing. */
    ParseTreeProperty<Function> functions;
    /** All of the conditionals encountered during parsing. */
    ParseTreeProperty<Conditional> conditionals;
    /** The stack of operation sequences generated by parsing. */
    Stack<OperationSequence> stack;
    /** A sorted list of errors encountered during parsing. */
    SortedList<Error> errors;
    /** A map that maps operation sequences to waypoints. */
    Map<Variable, OperationSequence> waypoints;
    /** The last function we encountered during parsing, this is nulled after each factory exit. */
    Function lastFunction = null;

    /**
     * Parses the supplied input using the otldRailroad and returns it after walking it
     * @param reader input to parse
     * @return walked otldRailroad
     * @throws IOException
     */
    public static otldRailroad parseFile(InputStream reader) throws IOException {
        otldErrorListener errorListener = new otldErrorListener();
        ANTLRInputStream stream = new ANTLRInputStream(reader);

        Lexer lexer = new otldLexer(stream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        TokenStream tokens = new CommonTokenStream(lexer);

        otldParser parser = new otldParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        ParseTree tree = parser.program();

        if (errorListener.getErrors().isEmpty()) {
            otldRailroad railroad = new otldRailroad();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(railroad, tree);

            return railroad;
        }
        return null;
    }

    /** Returns all of the errors encountered during parsing. */
    public SortedList getErrors() {
        return errors;
    }

    /** Returns the program. */
    public Program getProgram() {
        return city;
    }

    /** Returns the system type of that is identified by the passed string. */
    public Type getType(String ctxType) {
        switch (ctxType) {
            case "boolean":
                return Type.BOOL;
            case "int":
                return Type.INT;
            case "char":
                return Type.CHAR;
            default:
                return null;
        }
    }

    /** Returns the system array type of that is identified by the passed string. */
    public Type getArrType(String ctxType) {
        switch (ctxType) {
            case "boolean":
                return Type.BOOLARR;
            case "int":
                return Type.INTARR;
            case "char":
                return Type.CHARARR;
            default:
                return null;
        }
    }

    /**
     * Returns a variable assigned with the passed id if one is declared.
     * If the code requesting the variable is inside a factory body a platform
     * can be returned as well.
     * All other cases return {@code null};
     *
     * @param id of the variable
     * @return requestedVariable
     */
    public Variable getVariable(String id) {
        Variable returnVar = null;
        if (id.startsWith("platform")) {
            if (lastFunction != null) {
                try {
                    int index = Integer.parseInt(id.substring(7));
                    Variable[] platforms = lastFunction.getVariables();

                    if (index <= platforms.length && index > 0) {
                        returnVar = platforms[index - 1];
                    }
                } catch (NumberFormatException e) {
                   //let returnVar remain null and have calling visitor throw an error
                }
            }
        } else {
            returnVar = city.getVariable(id);
        }
        return returnVar;
    }

    @Override
    public void enterCity(otldParser.CityContext ctx) {
        city = new Program(ctx.ID().getText());
        stack.push(city.getBody());
    }

    @Override
    public void exitCity(otldParser.CityContext ctx) {
        stack.pop();
    }

    @Override
    public void enterDeftrain(otldParser.DeftrainContext ctx) {
        /*Train represents an array, arrays are as of yet still unsupported in our intermediate representation so this
        code isn't used.*/
        if (!ctx.ID().getText().startsWith("platform")) {
            try {
                city.addVariable(Variable.create(getArrType(ctx.CARGO().getText()), ctx.ID().getText(), null));
            } catch (VariableAlreadyDeclared variableAlreadyDeclared) {
                errors.add(new Error(ctx.ID().getSymbol().getLine(),
                        ctx.ID().getSymbol().getCharPositionInLine(),
                        ErrorMsg.VARALREADYDEFINED.getMessage()));
            }
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.RESERVEDNAME.getMessage()));
        }
    }

    @Override
    public void enterDefwagon(otldParser.DefwagonContext ctx) {
        if (!ctx.ID().getText().startsWith("platform")) {
            try {
                Type type = getType(ctx.CARGO().getText());

                if (type != null) {
                    city.addVariable(Variable.create(type, ctx.ID().getText(), null));
                } else {
                    errors.add(new Error(ctx.CARGO().getSymbol().getLine(),
                            ctx.CARGO().getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPENOTDEFINED.getMessage()));
                }
            } catch (VariableAlreadyDeclared variableAlreadyDeclared) {
                errors.add(new Error(ctx.ID().getSymbol().getLine(),
                        ctx.ID().getSymbol().getCharPositionInLine(),
                        ErrorMsg.VARALREADYDEFINED.getMessage()));
            }
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.RESERVEDNAME.getMessage()));
        }
    }

    @Override
    public void enterFactory(otldParser.FactoryContext ctx) {
        ArrayList<Type> types = new ArrayList<>(ctx.CARGO().size());
        for (TerminalNode n : ctx.CARGO()) {
            Type type = getType(n.getText());

            if (type != null) {
                types.add(type);
            } else {
                errors.add(new Error(ctx.CARGO().get(ctx.CARGO().indexOf(n)).getSymbol().getLine(),
                        ctx.CARGO().get(ctx.CARGO().indexOf(n)).getSymbol().getCharPositionInLine(),
                        ErrorMsg.TYPENOTDEFINED.getMessage()));
            }
        }

        try {
            Function function = new Function(ctx.ID().getText(), (Type[]) types.toArray());
            city.addFunction(function);
            functions.put(ctx.deffactory(), function);
            //Set the lastFunction to this so code that is part of the factory body can access it's platforms
            lastFunction = function;
        } catch (FunctionAlreadyDeclared functionAlreadyDeclared) {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.FACTALREADYDEFINED.getMessage()));
        }
    }

    @Override
    public void enterDeffactory(otldParser.DeffactoryContext ctx) {
        Function function = functions.get(ctx);
        stack.push(function.getBody());
    }

    @Override
    public void exitDeffactory(otldParser.DeffactoryContext ctx) {
        Return ret;
        if (getVariable(ctx.ID().getText()) != null) {
            ret = new Return(getVariable(ctx.ID().getText()));
            if (functions.get(ctx).getType().equals(ret.getSource().getType())) {
                stack.peek().add(ret);
            } else {
                errors.add(new Error(ctx.ID().getSymbol().getLine(),
                        ctx.ID().getSymbol().getCharPositionInLine(),
                        ErrorMsg.TYPEMISMATCH.getMessage()));
            }
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.VARNOTDEFINED.getMessage()));
        }

        stack.pop();
        //Reset the lastFunction to null so other parts of the code cannot access platforms
        lastFunction = null;
    }

    @Override
    public void enterDefsignal(otldParser.DefsignalContext ctx) {
        if (!ctx.ID().getText().startsWith("platform")) {
            try {
                city.addVariable(Variable.create(Type.BOOL, ctx.ID().getText(), "false"));
            } catch (VariableAlreadyDeclared variableAlreadyDeclared) {
                errors.add(new Error(ctx.ID().getSymbol().getLine(),
                        ctx.ID().getSymbol().getCharPositionInLine(),
                        ErrorMsg.VARALREADYDEFINED.getMessage()));
            }
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.RESERVEDNAME.getMessage()));
        }
    }

    @Override
    public void enterDefwaypoint(otldParser.DefwaypointContext ctx) {
        if (!ctx.ID().getText().startsWith("platform")) {
            try {
                Variable waypoint = Variable.create(Type.BOOL, ctx.ID().getText(), "false");
                OperationSequence ops = new OperationSequence();
                waypoints.put(waypoint, ops);

                stack.push(ops);
                city.addVariable(waypoint);
            } catch (VariableAlreadyDeclared variableAlreadyDeclared) {
                errors.add(new Error(ctx.ID().getSymbol().getLine(),
                        ctx.ID().getSymbol().getCharPositionInLine(),
                        ErrorMsg.VARALREADYDEFINED.getMessage()));
            }
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.RESERVEDNAME.getMessage()));
        }
    }

    @Override
    public void exitDefwaypoint(otldParser.DefwaypointContext ctx) {
        stack.pop();
    }

    @Override
    public void enterDefcircle(otldParser.DefcircleContext ctx) {
        Variable variable = getVariable(ctx.ID().getText());

        if (variable != null) {
            Loop loop = new Loop(variable);
            stack.peek().add(loop);
            stack.push(loop.getBody());
            //Add all the opsequence stored in the map to the condition body of the loop
            loop.getConditionBody().addAll(waypoints.get(variable));
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.VARNOTDEFINED.getMessage()));
        }
    }

    @Override
    public void exitDefcircle(otldParser.DefcircleContext ctx) {
        stack.pop();
    }

    @Override
    public void enterIfcond(otldParser.IfcondContext ctx) {
        Variable variable = getVariable(ctx.ID().getText());
        if (variable != null) {
            Conditional cond = new Conditional(variable);
            stack.peek().add(cond);
            for (otldParser.IfcondcaseContext c : ctx.ifcondcase()) {
                conditionals.put(c, cond);
            }
        }
    }

    @Override
    public void enterIfcondcase(otldParser.IfcondcaseContext ctx) {
        Conditional conditional = conditionals.get(ctx);
        switch (ctx.BOOLEAN().getText()) {
            case "red":
                stack.push(conditional.getBodyFalse());
                break;
            case "green":
                stack.push(conditional.getBodyFalse());
                break;
            default:
                errors.add(new Error(ctx.BOOLEAN().getSymbol().getLine(),
                        ctx.BOOLEAN().getSymbol().getCharPositionInLine(),
                        ErrorMsg.UNKNOWNVALUE.getMessage()));
        }
    }

    @Override
    public void exitIfcondcase(otldParser.IfcondcaseContext ctx) {
        stack.pop();
    }

    @Override
    public void enterStop(otldParser.StopContext ctx) {
        stack.peek().add(new Break());
    }

    @Override
    public void enterLoad(otldParser.LoadContext ctx) {
        //TODO Array support is missing
        //Get the previously defined variable
        Variable var = getVariable(ctx.ID().getText());
        //Check if it has been previously defined.
        if (var != null) {
            if (ctx.INTEGER() != null) {
                if (var.getType().equals(Type.INT)) {
                    stack.peek().add(var.createValueAssignment(Integer.valueOf(ctx.INTEGER().getText())));
                }
            } else if (ctx.BOOLEAN() != null) {
                if (var.getType().equals(Type.BOOL)) {
                    //Translate our green/red to true/false
                    Boolean boolval = true;
                    if (ctx.BOOLEAN().getText().equals("red")) {
                        boolval = false;
                    }
                    stack.peek().add(var.createValueAssignment(boolval));
                }
            } else if (ctx.CHARACTER() != null) {
                if (var.getType().equals(Type.CHAR)) {
                    stack.peek().add(var.createValueAssignment(ctx.CHARACTER().getText().charAt(0)));
                }
            }
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.VARNOTDEFINED.getMessage()));
        }

    }

    @Override
    public void enterTransfer(otldParser.TransferContext ctx) {
        //TODO implement for arrays
        Variable var0 = getVariable(ctx.ID().get(0).getText());
        Variable var1 = getVariable(ctx.ID().get(1).getText());

        if (var0 != null) {
            if (var1 != null) {
                try {
                    stack.peek().add(var1.createVariableAssignment(var0));
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
            } else {
                errors.add(new Error(ctx.ID().get(1).getSymbol().getLine(),
                        ctx.ID().get(1).getSymbol().getCharPositionInLine(),
                        ErrorMsg.VARNOTDEFINED.getMessage()));
            }
        } else {
            errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                    ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                    ErrorMsg.VARNOTDEFINED.getMessage()));
        }
    }

    @Override
    public void enterTransport(otldParser.TransportContext ctx) {
        ArrayList<Variable> vars = new ArrayList<>(ctx.ID().size());

        //Check if all of the provided arguments exist
        for (TerminalNode node : ctx.ID()) {
            if (getVariable(node.getText()) != null) {
                if (!node.equals(ctx.ID().get(-2))) {
                    vars.add(getVariable(node.getText()));
                }
            } else {
                errors.add(new Error(ctx.ID().get(ctx.ID().indexOf(node)).getSymbol().getLine(),
                        ctx.ID().get(ctx.ID().indexOf(node)).getSymbol().getCharPositionInLine(),
                        ErrorMsg.VARNOTDEFINED.getMessage()));
            }
        }

        //Second to last variable is the name of the called function
        String functionID = ctx.ID().get(ctx.ID().size() - 2).getText();
        Application appl;

        switch (functionID) {
            case "addition":
                try {
                    appl = new Application(Operator.ADDITION, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "subtraction":
                try {
                    appl = new Application(Operator.SUBTRACTION, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "multiplication":
                try {
                    appl = new Application(Operator.MULTIPLICATION, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "division":
                try {
                    appl = new Application(Operator.DIVISION, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "modulus":
                try {
                    appl = new Application(Operator.MODULUS, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "uminus":
                try {
                    appl = new Application(vars.get(0).getType() == Type.BOOL ? Operator.NOT : Operator.UMINUS, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "land":
                try {
                    appl = new Application(Operator.LAND, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "lor":
                try {
                    appl = new Application(Operator.LOR, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "lxor":
                try {
                    appl = new Application(Operator.LXOR, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "and":
                try {
                    appl = new Application(Operator.AND, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "or":
                try {
                    appl = new Application(Operator.OR, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "not":
                try {
                    appl = new Application(Operator.NOT, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "complte":
                try {
                    appl = new Application(Operator.COMPLTE, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "compgte":
                try {
                    appl = new Application(Operator.COMPGTE, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "complt":
                try {
                    appl = new Application(Operator.COMPLT, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "compgt":
                try {
                    appl = new Application(Operator.COMPGT, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "equals":
                try {
                    appl = new Application(Operator.EQUALS, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            case "nequals":
                try {
                    appl = new Application(Operator.NEQUALS, (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                            ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
                break;
            //This means we have a custom function
            default:
                Function func = city.getFunction(functionID);
                //If the custom function exists then call it
                if (func != null) {
                    try {
                        Call call = new Call(func, (Variable[]) vars.toArray());
                        stack.peek().add(call);
                    } catch (TypeMismatch typeMismatch) {
                        errors.add(new Error(ctx.ID().get(0).getSymbol().getLine(),
                                ctx.ID().get(0).getSymbol().getCharPositionInLine(),
                                ErrorMsg.TYPEMISMATCH.getMessage()));
                    }
                } else {
                    errors.add(new Error(ctx.ID().get(ctx.ID().size() - 2).getSymbol().getLine(),
                            ctx.ID().get(ctx.ID().size() - 2).getSymbol().getCharPositionInLine(),
                            ErrorMsg.FACTUNDEFINED.getMessage()));
                }
        }
    }

    @Override
    public void enterInvert(otldParser.InvertContext ctx) {
        Variable var = getVariable(ctx.ID().getText());

        if (var != null) {
            if (var.getType().equals(Type.BOOL)) {
                try {
                    Application apl = new Application(Operator.NOT, var, var);
                    stack.peek().add(apl);
                } catch (TypeMismatch typeMismatch) {
                    errors.add(new Error(ctx.ID().getSymbol().getLine(),
                            ctx.ID().getSymbol().getCharPositionInLine(),
                            ErrorMsg.TYPEMISMATCH.getMessage()));
                }
            } else {
                errors.add(new Error(ctx.ID().getSymbol().getLine(),
                        ctx.ID().getSymbol().getCharPositionInLine(),
                        ErrorMsg.TYPEMISMATCH.getMessage()));
            }
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.VARNOTDEFINED.getMessage()));
        }
    }

    @Override
    public void enterUnarymin(otldParser.UnaryminContext ctx) {
        Variable var = getVariable(ctx.ID().getText());

        if (var != null) {
            try {
                Application appl = new Application(Operator.UMINUS, var, var);
                stack.peek().add(appl);
            } catch (TypeMismatch typeMismatch) {
                errors.add(new Error(ctx.ID().getSymbol().getLine(),
                        ctx.ID().getSymbol().getCharPositionInLine(),
                        ErrorMsg.TYPEMISMATCH.getMessage()));
            }
        }
    }

    @Override
    public void enterWrite(otldParser.WriteContext ctx) {
        Variable src = getVariable(ctx.ID().getText());
        if (src != null) {
            Output output = new Output(ctx.STRING().getText(), src);
            stack.peek().add(output);
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.VARNOTDEFINED.getMessage()));
        }
    }

    @Override
    public void enterInput(otldParser.InputContext ctx) {
        Variable dest = getVariable(ctx.ID().getText());
        if (dest != null) {
            Input input = new Input(ctx.STRING().getText(), dest);
            stack.peek().add(input);
        } else {
            errors.add(new Error(ctx.ID().getSymbol().getLine(),
                    ctx.ID().getSymbol().getCharPositionInLine(),
                    ErrorMsg.VARNOTDEFINED.getMessage()));
        }
    }
}
