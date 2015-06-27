package otld.otld.intermediate;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class VariableTest {

    @Test
    public void testGetId() throws Exception {
        Variable v = Variable.create(Type.BOOL, "test", null);
        assertEquals("test", v.getId());
    }

    @Test
    public void testGetType() throws Exception {
        Variable v = Variable.create(Type.BOOL, "test", null);
        assertEquals(Type.BOOL, v.getType());
    }

    @Test
    public void testGetInitialValue() throws Exception {
        Variable v = Variable.create(Type.BOOL, "test", null);
        assertEquals(null, v.getInitialValue());

        Variable w = Variable.create(Type.BOOL, "test", "true");
        assertEquals(true, w.getInitialValue());
    }

    @Test
    public void testCreate() throws Exception {
        // Boolean
        Variable bool = Variable.create(Type.BOOL, "boolean", "true");
        assertEquals(Type.BOOL, bool.getType());
        assertEquals("boolean", bool.getId());
        assertEquals(true, bool.getInitialValue());

        // Integer
        Variable integer = Variable.create(Type.INT, "integer", "42");
        assertEquals(Type.INT, integer.getType());
        assertEquals("integer", integer.getId());
        assertEquals(42, integer.getInitialValue());

        // Character
        Variable character = Variable.create(Type.CHAR, "character", "b");
        assertEquals(Type.CHAR, character.getType());
        assertEquals("character", character.getId());
        assertEquals('b', character.getInitialValue());
    }
}