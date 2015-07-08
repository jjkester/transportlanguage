package otld.otld.parsing;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import otld.otld.grammar.otldBaseListener;
import otld.otld.grammar.otldParser;
import otld.otld.intermediate.*;
import otld.otld.intermediate.exceptions.FunctionAlreadyDeclared;
import otld.otld.intermediate.exceptions.TypeMismatch;
import otld.otld.intermediate.exceptions.VariableAlreadyDeclared;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Base visitor for the OTLD intermediate representation.
 */
public class otldRailroad extends otldBaseListener {

    private Program city;
    ParseTreeProperty<Function> functions;
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
    public void enterProgram(otldParser.ProgramContext ctx) { super.enterProgram(ctx); }

    @Override
    public void exitProgram(otldParser.ProgramContext ctx) { super.exitProgram(ctx); }

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

}
