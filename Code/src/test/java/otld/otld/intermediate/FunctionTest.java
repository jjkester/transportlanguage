package otld.otld.intermediate;

import org.junit.Test;

import static org.junit.Assert.*;

public class FunctionTest {

    @Test
    public void testGetId() throws Exception {
        Function f = new Function("f", Type.INT, Type.BOOL);
        assertEquals("f", f.getId());
    }

    @Test
    public void testValidateInput() throws Exception {
        Function f = new Function("f", Type.INT, Type.BOOL);
        assertTrue(f.validateInput(Variable.create(Type.INT, "v", null)));
    }

    @Test
    public void testValidateInputError() throws Exception {
        Function f = new Function("f", Type.INT, Type.BOOL);
        assertFalse(f.validateInput(Variable.create(Type.BOOL, "v", null)));
        assertFalse(f.validateInput(Variable.create(Type.INT, "v", null), Variable.create(Type.BOOL, "w", null)));
    }

    @Test
    public void testGetArgs() throws Exception {
        Function f = new Function("f", Type.INT, Type.BOOL, Type.CHAR);
        Type[] args = {Type.INT, Type.BOOL};
        assertArrayEquals(args, f.getArgTypes());
    }

    @Test
    public void testGetType() throws Exception {
        Function f = new Function("f", Type.INT, Type.BOOL, Type.CHAR);
        assertEquals(Type.CHAR, f.getType());
    }
}
