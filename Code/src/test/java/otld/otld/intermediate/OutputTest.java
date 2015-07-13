package otld.otld.intermediate;

import org.junit.Test;

import static org.junit.Assert.*;

public class OutputTest {

    @Test
    public void testGetDescription() throws Exception {
        Variable<Integer> intVar = new Variable<Integer>(Type.INT, "i", null);
        Output out = new Output("Value: ", intVar);
        assertEquals("Value: ", out.getDescription());
    }

    @Test
    public void testGetSource() throws Exception {
        Variable<Integer> intVar = new Variable<Integer>(Type.INT, "i", null);
        Output out = new Output("Value: ", intVar);
        assertEquals(intVar, out.getSource());
    }
}
