package otld.otld.intermediate;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConditionalTest {

    @Test
    public void testGetCondition() throws Exception {
        Variable v = Variable.create(Type.BOOL, "v", "false");
        Conditional c = new Conditional(v);
        assertEquals(v, c.getCondition());
    }
}