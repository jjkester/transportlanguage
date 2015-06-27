package otld.otld.intermediate;

import org.junit.Test;

import static org.junit.Assert.*;

public class OperatorTest {

    @Test
    public void testGetSymbol() throws Exception {
        assertEquals("&&", Operator.AND.getSymbol());
        assertEquals("+", Operator.ADDITION.getSymbol());
        assertEquals("<=", Operator.COMPLTE.getSymbol());
    }

    @Test
    public void testGetArgs() throws Exception {
        Type[] args = {Type.ANY, Type.ANY};
        assertArrayEquals(args, Operator.EQUALS.getArgs());
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals(Type.BOOL, Operator.EQUALS.getType());
    }

    @Test
    public void testValidateInput() throws Exception {
        Variable v = Variable.create(Type.INT, "v", "10");
        Variable w = Variable.create(Type.BOOL, "w", "false");
        assertTrue(Operator.EQUALS.validateInput(v, w));
        assertFalse(Operator.ADDITION.validateInput(v, w));
    }
}