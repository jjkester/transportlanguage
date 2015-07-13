package otld.otld.parsing;

import com.sun.org.apache.xpath.internal.operations.Bool;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Base visitor for the OTLD intermediate representation.
 */
public class otldRailroad extends otldBaseListener {

    private Program city;
    ParseTreeProperty<Function> functions;
    ParseTreeProperty<Conditional> conditionals;
    Stack<OperationSequence> stack;

    public Type getType(String ctxType) {

        switch (ctxType) {
            case "boolean" :
                return Type.BOOL;

            case "int" :
                return Type.INT;

            case "char" :
                return Type.CHAR;

            default:
                System.out.println("Unknown type");
                //TODO throw type error, undefined type!
                return Type.ANY;
        }
    }

    public Type getArrType(String ctxType) {

        switch (ctxType) {
            case "boolean" :
                return Type.BOOLARR;

            case "int" :
                return Type.INTARR;

            case "char" :
                return Type.CHARARR;

            default:
                System.out.println("Unknown type");
                //TODO throw type error, undefined type!
                return Type.ANY;
        }
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
        try {
            city.addVariable(Variable.create(getArrType(ctx.CARGO().getText()), ctx.ID().getText(), null));
        } catch (VariableAlreadyDeclared variableAlreadyDeclared) {
            System.out.println("Variable already declared");
            //TODO error handling
        }

    }

    @Override
    public void enterDefwagon(otldParser.DefwagonContext ctx) {
        try {
            city.addVariable(Variable.create(getType(ctx.CARGO().getText()), ctx.ID().getText(), null));
        } catch (VariableAlreadyDeclared variableAlreadyDeclared) {
            System.out.println("Variable already declared");
            //TODO error handling
        }
    }

    @Override
    public void enterFactory(otldParser.FactoryContext ctx) {
        ArrayList<Type> types = new ArrayList<>(ctx.CARGO().size());
        for (TerminalNode n : ctx.CARGO()) {
            types.add(getType(n.getText()));
        }

        try {
            Function function = new Function(ctx.ID().getText(), (Type[]) types.toArray());
            city.addFunction(function);
            functions.put(ctx.deffactory(), function);
        } catch (FunctionAlreadyDeclared functionAlreadyDeclared) {
            System.out.println("Function has already been declared");
            //TODO error handling
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
        if (city.getVariable(ctx.ID().getText())!= null) {
            ret = new Return(city.getVariable(ctx.ID().getText()));
            if (!functions.get(ctx).getType().equals(ret.getSource().getType())) {
                // TODO handle type error
            }
            stack.peek().add(ret);
        } else {
            // TODO Handle error variable undeclared.
        }

        stack.pop();
    }

    @Override
    public void enterDefsignal(otldParser.DefsignalContext ctx) {
        try {
            city.addVariable(Variable.create(Type.BOOL, ctx.ID().getText(), "false"));
        } catch (VariableAlreadyDeclared variableAlreadyDeclared) {
            System.out.println("Variable already declared");
            //TODO error handling
        }
    }

    @Override
    public void enterDefwaypoint(otldParser.DefwaypointContext ctx) {

        try {
            city.addFunction(new Function(ctx.ID().getText(), Type.BOOL));
            city.addVariable(Variable.create(Type.BOOL, ctx.ID().getText(), "false"));
            functions.put(ctx.ID(), null);
        } catch (FunctionAlreadyDeclared functionAlreadyDeclared) {
            System.out.println("Function has already been declared");
            //TODO error handling
        } catch (VariableAlreadyDeclared variableAlreadyDeclared) {
            System.out.println("Variable has already been declared");
            //TODO error handling
        }
    }

    @Override
    public void exitDefwaypoint(otldParser.DefwaypointContext ctx) {
        Return ret;
        if (city.getVariable(ctx.ID().getText())!= null) {
            ret = new Return(city.getVariable(ctx.ID().getText()));
            if (!functions.get(ctx).getType().equals(ret.getSource().getType())) {
                // TODO handle type error
            }
            stack.peek().add(ret);
        } else {
            // TODO Handle error variable undeclared.
        }
        stack.pop();
    }

    @Override
    public void enterDefcircle(otldParser.DefcircleContext ctx) {
        Variable variable = city.getVariable(ctx.ID().getText());
        if (variable != null) {
            try {
                stack.peek().add(new Call(city.getFunction(ctx.ID().getText())));
                Loop loop = new Loop(variable);
                stack.peek().add(loop);
                stack.push(loop.getBody());

            } catch (TypeMismatch typeMismatch) {
                System.out.println("Type mismatch!");
                //TODO error handling
            }
        }
    }

    @Override
    public void exitDefcircle(otldParser.DefcircleContext ctx) {
        stack.pop();
    }

    @Override
    public void enterIfcond(otldParser.IfcondContext ctx) {
        Variable variable = city.getVariable(ctx.ID().getText());
        if (variable != null) {
            Conditional cond = new Conditional(variable);
            for (otldParser.IfcondcaseContext c : ctx.ifcondcase()) {
                conditionals.put(c, cond);
            }
        }
    }

    @Override
    public void enterIfcondcase(otldParser.IfcondcaseContext ctx) {
        Conditional conditional = conditionals.get(ctx);
        switch (ctx.BOOLEAN().getText()) {
            case "red" :
                stack.push(conditional.getBodyFalse());
                break;
            case "green" :
                stack.push(conditional.getBodyFalse());
                break;
            default :
                System.out.println("Unknown boolean value");
                //TODO error handling
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
        Variable var = city.getVariable(ctx.ID().getText());
        //Check if it has been previously defined.
        if (var != null) {

            if (ctx.INTEGER() != null) {
                if (var.getType().equals(Type.INT)) {
                    stack.peek().add(var.createValueAssignment(Integer.valueOf(ctx.INTEGER().getText())));
                }
            }

            else if (ctx.BOOLEAN() != null) {
                if (var.getType().equals(Type.BOOL)) {
                    //Translate our green/red to true/false
                    Boolean boolval = true;
                    if (ctx.BOOLEAN().getText().equals("red")) {
                        boolval = false;
                    }
                    stack.peek().add(var.createValueAssignment(boolval));
                }
            }

            else if (ctx.CHARACTER() != null) {
                if (var.getType().equals(Type.CHAR)) {
                    stack.peek().add(var.createValueAssignment(ctx.CHARACTER().getText().charAt(0)));
                }
            }
        }

    }

    @Override
    public void enterTransfer(otldParser.TransferContext ctx) {
        //TODO implement for arrays
        Variable var0 = city.getVariable(ctx.ID().get(0).getText());
        Variable var1 = city.getVariable(ctx.ID().get(1).getText());

        if (var0 != null && var1 != null) {
            try {
                stack.peek().add(var1.createVariableAssignment(var0));
            } catch (TypeMismatch typeMismatch) {
                System.out.println("Type mismatch error!");
                //TODO error handling
            }
        }
    }

    @Override
    public void enterTransport(otldParser.TransportContext ctx) {
        ArrayList<Type> vars = new ArrayList<>(ctx.ID().size());

            //This means we have a predefined function
            if (ctx.OP() != null) {
                //Check if all of the provided arguments exist
                for (TerminalNode node : ctx.ID()) {
                    if (city.getVariable(node.getText()) == null) {
                        System.out.println(node.getText() + " is null!");
                        //TODO create a list of errors
                    }
                    vars.add(getType(node.getText()));
                }

                try {
                    Application appl = new Application(Operator.valueOf(ctx.OP().getText()), (Variable[]) vars.toArray());
                    stack.peek().add(appl);
                } catch (TypeMismatch typeMismatch) {
                    System.out.println("Type Mismatch!");
                    //TODO error handling
                }
            }

            //This means we have a custom function
            if (ctx.OP() == null) {
                //Remove the second to last argument since this is actually the function name
                vars.remove(ctx.ID().size()-2);

                //Check if all of the provided arguments exist
                for (TerminalNode node : ctx.ID()) {
                    if (city.getVariable(node.getText()) == null) {
                        System.out.println(node.getText() + " is null!");
                        //TODO create a list of errors
                    }
                    vars.add(getType(node.getText()));
                }

                Function func = city.getFunction(ctx.ID().get(ctx.ID().size()-2).getText());
                //If the custom function exists then call it
                if (func != null) {
                    try {
                        Call call = new Call(func, (Variable[]) vars.toArray());
                        stack.peek().add(call);
                    } catch (TypeMismatch typeMismatch) {
                        System.out.println("Type Mismatch!");
                        //TODO error handling
                    }
                } else {
                    System.out.println("This Factory is undefined!");
                    //TODO error handling
                }
            }
    }

    @Override
    public void enterInvert(otldParser.InvertContext ctx) {
        Variable var = city.getVariable(ctx.ID().getText());

        if (var != null) {
            if (var.getType().equals(Type.BOOL)) {
                try {
                    Application apl = new Application(Operator.NOT, var, var);
                    stack.peek().add(apl);
                } catch (TypeMismatch typeMismatch) {
                    System.out.println("Type mismatch!");
                    //TODO error handling
                }
            } else {
                System.out.println("Invert operation is not applicable for this type!");
                //TODO error handling
            }
        } else {
            System.out.println("This variable is undefined!");
            //TODO error handling
        }
    }

    @Override
    public void enterUnarymin(otldParser.UnaryminContext ctx) {
        Variable var = city.getVariable(ctx.ID().getText());

        if (var != null) {
            try {
                Application appl = new Application(Operator.UMINUS, var, var);
                stack.peek().add(appl);
            } catch (TypeMismatch typeMismatch) {
                System.out.println("Type mismatch!");
                //TODO error handling
            }
        }
    }

    @Override
    public void enterWrite(otldParser.WriteContext ctx) {

        super.enterWrite(ctx);
    }






}
