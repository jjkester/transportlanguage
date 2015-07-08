package otld.otld.parsing;

import org.antlr.v4.runtime.tree.TerminalNode;
import otld.otld.grammar.otldBaseListener;
import otld.otld.grammar.otldParser;
import otld.otld.intermediate.Function;
import otld.otld.intermediate.Program;
import otld.otld.intermediate.Type;
import otld.otld.intermediate.Variable;
import otld.otld.intermediate.exceptions.FunctionAlreadyDeclared;
import otld.otld.intermediate.exceptions.VariableAlreadyDeclared;

import java.util.ArrayList;
import java.util.List;

/**
 * Base visitor for the OTLD intermediate representation.
 */
public class otldRailroad extends otldBaseListener {

    private Program city;

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
    public void exitDeffactory(otldParser.DeffactoryContext ctx) {
        super.exitDeffactory(ctx);
    }

    @Override
    public void enterProgram(otldParser.ProgramContext ctx) { super.enterProgram(ctx); }

    @Override
    public void exitProgram(otldParser.ProgramContext ctx) {
        super.exitProgram(ctx);
    }

    @Override
    public void enterCity(otldParser.CityContext ctx) {
        city = new Program(ctx.ID().getText());
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

        Function func = new Function(ctx.ID().getText(), (Type[]) types.toArray());
    }

    @Override
    public void exitFactory(otldParser.FactoryContext ctx) {
        super.exitFactory(ctx);
        //TODO
    }

    @Override
    public void enterDeffactory(otldParser.DeffactoryContext ctx) {
        super.enterDeffactory(ctx);
        //TODO
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
        } catch (FunctionAlreadyDeclared functionAlreadyDeclared) {
            System.out.println("Function has already been declared");
            //TODO error handling
        }
    }
}
