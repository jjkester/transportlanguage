package otld.otld.jvm;

import jdk.internal.org.objectweb.asm.util.ASMifier;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import otld.otld.intermediate.*;
import otld.otld.intermediate.exceptions.FunctionAlreadyDeclared;
import otld.otld.intermediate.exceptions.TypeMismatch;
import otld.otld.intermediate.exceptions.VariableAlreadyDeclared;

import java.io.FileOutputStream;

public class JVMCompilerTest {

    @Test
    public void testCompile() throws Exception {
        Program program = this.getProgram();
        byte[] bytecode = new JVMCompiler(true).compile(program);
        FileOutputStream fos = new FileOutputStream(String.format("/tmp/%s.class", program.getId()));
        fos.write(bytecode);
        fos.close();
    }

    private Program getProgram() {
        // Define program
        Program program = new Program("Test");

        // Add some variables
        try {
            program.addVariable(Variable.create(Type.INT, "x", "10"));
            program.addVariable(Variable.create(Type.INT, "y", "0"));
            program.addVariable(Variable.create(Type.INT, "z", "0"));
            program.addVariable(Variable.create(Type.BOOL, "a", "false"));
            program.addVariable(Variable.create(Type.BOOL, "b", "false"));
        } catch (VariableAlreadyDeclared variableAlreadyDeclared) {
            variableAlreadyDeclared.printStackTrace();
        }

        // Add a function
        try {
            Function f = new Function("add10", Type.INT, Type.INT);
            program.addFunction(f);
            f.getBody().add(new Application(Operator.ADDITION, program.getVariable("x"), f.getVariables()[0], f.getVariables()[0]));
            f.getBody().add(new Return(f.getVariables()[0]));
        } catch (TypeMismatch typeMismatch) {
            typeMismatch.printStackTrace();
        } catch (FunctionAlreadyDeclared functionAlreadyDeclared) {
            functionAlreadyDeclared.printStackTrace();
        }

        // Set body
        try {
            program.getBody().add(program.getVariable("y").createValueAssignment(5));
            program.getBody().add(new Call(program.getFunction("add10"), program.getVariable("y"), program.getVariable("z")));
            program.getBody().add(new Application(Operator.EQUALS, program.getVariable("x"), program.getVariable("z"), program.getVariable("a")));
            program.getBody().add(program.getVariable("x").createVariableAssignment(program.getVariable("z")));
            program.getBody().add(new Application(Operator.EQUALS, program.getVariable("x"), program.getVariable("z"), program.getVariable("b")));
            program.getBody().add(new Break());
        } catch (TypeMismatch typeMismatch) {
            typeMismatch.printStackTrace();
        }

        return program;
    }
}