package otld.otld.intermediate;

import org.junit.Test;
import otld.otld.intermediate.exceptions.TypeMismatch;

import static org.junit.Assert.*;

public class VariableAssignmentTest {

    @Test
    public void testGetDestination() throws Exception {
        Variable v = Variable.create(Type.INT, "v", null);
        Variable w = Variable.create(Type.INT, "w", "42");
        VariableAssignment vw = new VariableAssignment(v, w);
        assertEquals(v, vw.getTarget());
    }

    @Test
    public void testGetSource() throws Exception {
        Variable v = Variable.create(Type.INT, "v", null);
        Variable w = Variable.create(Type.INT, "w", "42");
        VariableAssignment vw = new VariableAssignment(v, w);
        assertEquals(w, vw.getSource());
    }

    @Test(expected = TypeMismatch.class)
    public void testGetSourceError() throws Exception {
        Variable v = Variable.create(Type.INT, "v", null);
        Variable w = Variable.create(Type.BOOL, "w", "false");
        VariableAssignment vw = new VariableAssignment(v, w);
    }
}
