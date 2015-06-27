package otld.otld.intermediate;

import org.junit.Test;

import static org.junit.Assert.*;

public class TypeTest {

    @Test
    public void testWorksWith() throws Exception {
        assertTrue(Type.INT.worksWith(Type.INT));
        assertTrue(Type.BOOL.worksWith(Type.BOOL));
        assertFalse(Type.INT.worksWith(Type.BOOL));
        assertFalse(Type.BOOL.worksWith(Type.INT));
        assertTrue(Type.ANY.worksWith(Type.INT));
        assertTrue(Type.ANY.worksWith(Type.BOOL));
        assertTrue(Type.ANY.worksWith(Type.CHAR));
        assertTrue(Type.CHAR.worksWith(Type.ANY));
    }
}