package otld.otld.intermediate;

import org.junit.Test;
import otld.otld.intermediate.exceptions.TypeMismatch;

import static org.junit.Assert.*;

public class LoopTest {

    @Test
    public void testGetCondition() throws Exception {
        Variable v = Variable.create(Type.BOOL, "v", "true");
        Loop l = new Loop(v);
        assertEquals(v, l.getCondition());
    }
}