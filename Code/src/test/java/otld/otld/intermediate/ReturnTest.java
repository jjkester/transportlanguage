package otld.otld.intermediate;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReturnTest {

    @Test
    public void testGetSource() throws Exception {
        Variable<Integer> intVar = new Variable<Integer>(Type.INT, "i", null);
        Return ret = new Return(intVar);
        assertEquals(intVar, ret.getSource());
    }
}
