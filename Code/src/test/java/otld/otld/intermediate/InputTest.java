package otld.otld.intermediate;

import org.junit.Test;

import static org.junit.Assert.*;

public class InputTest {

    @Test
    public void testGetQuery() throws Exception {
        Variable<Integer> intVar = new Variable<Integer>(Type.INT, "i", null);
        Input in = new Input("Enter a number: ", intVar);
        assertEquals("Enter a number: ", in.getQuery());
    }

    @Test
    public void testGetDestination() throws Exception {
        Variable<Integer> intVar = new Variable<Integer>(Type.INT, "i", null);
        Input in = new Input("Enter a number: ", intVar);
        assertEquals(intVar, in.getTarget());
    }
}
