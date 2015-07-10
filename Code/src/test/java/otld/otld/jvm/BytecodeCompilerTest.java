package otld.otld.jvm;

import org.junit.Before;
import org.junit.Test;
import otld.otld.intermediate.*;

import java.io.FileOutputStream;

import static org.junit.Assert.*;

public class BytecodeCompilerTest {

    @Test
    public void testCompile() throws Exception {
        Program program = new Program("Empty");
        BytecodeCompiler compiler = new BytecodeCompiler(program);

        assertFalse(compiler.isCompiled());
        assertArrayEquals(new byte[0], compiler.asByteArray());

        compiler.compile();

        assertTrue(compiler.isCompiled());
        assertTrue(compiler.asByteArray().length > 0);
    }

    @Test
    public void testVariableLocation() throws Exception {
        Program program = new Program("Empty");
        BytecodeCompiler compiler = new BytecodeCompiler(program);

        Variable x = Variable.create(Type.INT, "x", null);
        Variable y = Variable.create(Type.INT, "y", null);
        Variable z = Variable.create(Type.INT, "z", null);

        compiler.setVariableLocation(x, -1);
        compiler.setVariableLocation(z, 1);

        assertEquals(-1, compiler.getVariableLocation(x));
        assertEquals(-1, compiler.getVariableLocation(y));
        assertEquals(1, compiler.getVariableLocation(z));
    }

    @Test
    public void testProgram() throws Exception {
        // Define new program
        Program program = new Program("Countdown");

        // Define variables
        Variable var_i = Variable.create(Type.INT, "i", null);
        Variable var_zero = Variable.create(Type.INT, "zero", "0");
        Variable var_one = Variable.create(Type.INT, "one", "1");
        Variable var_continue = Variable.create(Type.BOOL, "continue", null);
        Variable var_stop = Variable.create(Type.BOOL, "stop", null);
        program.addVariable(var_i);
        program.addVariable(var_zero);
        program.addVariable(var_one);
        program.addVariable(var_continue);
        program.addVariable(var_stop);

        // Define function
        Function func_count = new Function("count", Type.INT, Type.INT);
        program.addFunction(func_count);

        func_count.getBody().add(new Output("", func_count.getVariables()[0]));
        func_count.getBody().add(new Return(var_zero));

        program.getBody().add(new Input("Count down from <int>: ", var_i));
        program.getBody().add(new Input("Stop at zero? <boolean>: ", var_stop));
        Application appl_stop = new Application(Operator.NOT, var_stop, var_stop);
        Application appl_continue = new Application(Operator.COMPGTE, var_i, var_zero, var_continue);
        Application appl_continue2 = new Application(Operator.OR, var_continue, var_stop, var_continue);
        program.getBody().add(appl_stop);
        program.getBody().add(appl_continue);
        program.getBody().add(appl_continue2);
        Loop loop_j = new Loop(var_continue);
        loop_j.getBody().add(new Call(func_count, var_i, var_zero));
        loop_j.getBody().add(new Application(Operator.SUBTRACTION, var_i, var_one, var_i));
        loop_j.getBody().add(appl_continue);
        loop_j.getBody().add(appl_continue2);
        program.getBody().add(loop_j);

        // Compile program
        // Tests for exceptions
        BytecodeCompiler compiler = new BytecodeCompiler(program);
        compiler.compileDebug();

        // Write to file for inspection
        FileOutputStream fos = new FileOutputStream(String.format("%s/%s.class", System.getProperty("java.io.tmpdir"), program.getId()));
        fos.write(compiler.asByteArray());
        fos.close();
    }
}
