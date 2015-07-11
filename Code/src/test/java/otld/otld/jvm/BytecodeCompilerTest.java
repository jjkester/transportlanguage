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
        Program program = new Program("Test");

        // Add some variables
        Variable<Integer> zero = Variable.create(Type.INT, "zero", "0");
        Variable<Integer> one = Variable.create(Type.INT, "one", "1");
        Variable<Integer> x = Variable.create(Type.INT, "x", "0");
        Variable<Integer> y = Variable.create(Type.INT, "y", "0");
        Variable<Integer> z = Variable.create(Type.INT, "z", "0");
        Variable<Boolean> a = Variable.create(Type.BOOL, "a", "false");
        Variable<Boolean> b = Variable.create(Type.BOOL, "b", "false");
        program.addVariable(zero);
        program.addVariable(one);
        program.addVariable(x);
        program.addVariable(y);
        program.addVariable(z);
        program.addVariable(a);
        program.addVariable(b);

        // Define new function
        Function fib = new Function("fib", Type.INT, Type.INT);
        program.addFunction(fib);

        // Build new function body (fib)
        Function isZero = new Function("is0", Type.INT, Type.BOOL);
        program.addFunction(isZero);
        isZero.getBody().add(new Application(Operator.EQUALS, isZero.getVariables()[0], zero, a));
        isZero.getBody().add(new Return(a));

        Function isOne = new Function("is1", Type.INT, Type.BOOL);
        program.addFunction(isOne);
        isOne.getBody().add(new Application(Operator.EQUALS, isOne.getVariables()[0], one, b));
        isOne.getBody().add(new Return(b));

        Conditional ifZero = new Conditional(a);
        Conditional ifOne = new Conditional(b);

        ifOne.getBodyTrue().add(new Return(one));

        ifOne.getBodyFalse().add(new Application(Operator.SUBTRACTION, fib.getVariables()[0], one, z));
        ifOne.getBodyFalse().add(new Call(fib, z, x));
        ifOne.getBodyFalse().add(new Application(Operator.SUBTRACTION, z, one, z));
        ifOne.getBodyFalse().add(new Call(fib, z, y));
        ifOne.getBodyFalse().add(new Application(Operator.ADDITION, x, y, z));
        ifOne.getBodyFalse().add(new Return(z));

        ifZero.getBodyFalse().add(new Call(isOne, fib.getVariables()[0], b));
        ifZero.getBodyFalse().add(ifOne);

        fib.getBody().add(new Call(isZero, fib.getVariables()[0], a));
        fib.getBody().add(ifZero);
        fib.getBody().add(new Return(zero));

        // Main body (calculate 10th fibonacci number)
        program.getBody().add(z.createValueAssignment(10));
        program.getBody().add(new Call(fib, z, z));

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
