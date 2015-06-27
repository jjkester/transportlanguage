package otld.otld.intermediate;

import org.junit.Test;
import otld.otld.intermediate.exceptions.FunctionAlreadyDeclared;
import otld.otld.intermediate.exceptions.VariableAlreadyDeclared;

import static org.junit.Assert.*;

public class ProgramTest {

    @Test
    public void testGetId() throws Exception {
        Program program = new Program("test");
        assertEquals("test", program.getId());
    }

    @Test
    public void testGetVariable() throws Exception {
        Program program = new Program("test");
        Variable v = Variable.create(Type.INT, "v", "10");
        program.addVariable(v);
        assertEquals(v, program.getVariable("v"));
    }

    @Test(expected = VariableAlreadyDeclared.class)
    public void testAddVariable() throws Exception {
        Program program = new Program("test");
        Variable v = Variable.create(Type.INT, "v", "10");
        program.addVariable(v);
        program.addVariable(Variable.create(Type.INT, "v", "10"));
    }

    @Test
    public void testGetFunction() throws Exception {
        Program program = new Program("test");
        Function f = new Function("f", Type.INT, Type.INT);
        program.addFunction(f);
        assertEquals(f, program.getFunction("f"));
    }

    @Test(expected = FunctionAlreadyDeclared.class)
    public void testAddFunction() throws Exception {
        Program program = new Program("test");
        Function f = new Function("f", Type.INT, Type.INT);
        program.addFunction(f);
        program.addFunction(new Function("f", Type.BOOL));
    }
}