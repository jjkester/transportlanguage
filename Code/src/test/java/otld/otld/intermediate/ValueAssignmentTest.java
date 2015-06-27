package otld.otld.intermediate;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValueAssignmentTest {

    @Test
    public void testGetValue() throws Exception {
        Variable v = Variable.create(Type.INT, "v", null);
        ValueAssignment v42 = new ValueAssignment<Integer>(v, 42);
        assertEquals(42, v42.getValue());
    }

    @Test
    public void testGetDestination() throws Exception {
        Variable v = Variable.create(Type.INT, "v", null);
        ValueAssignment v42 = new ValueAssignment<Integer>(v, 42);
        assertEquals(v, v42.getDestination());
    }
}