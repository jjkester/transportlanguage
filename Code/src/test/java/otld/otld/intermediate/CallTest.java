package otld.otld.intermediate;

import org.junit.Test;
import otld.otld.intermediate.exceptions.TypeMismatch;

import static org.junit.Assert.*;

public class CallTest {

    @Test
    public void testGetFunction() throws Exception {
        Function f = new Function("f", Type.INT, Type.INT);
        Variable v = Variable.create(Type.INT, "v", "10");
        Variable w = Variable.create(Type.INT, "w", null);
        Call c = new Call(f, v, w);
        assertEquals(f, c.getFunction());
    }

    @Test
    public void testGetArgs() throws Exception {
        Function f = new Function("f", Type.INT, Type.INT);
        Variable v = Variable.create(Type.INT, "v", "10");
        Variable w = Variable.create(Type.INT, "w", null);
        Call c = new Call(f, v, w);
        Variable[] args = {v};
        assertArrayEquals(args, c.getArgs());
    }

    @Test
    public void testGetVariable() throws Exception {
        Function f = new Function("f", Type.INT, Type.INT);
        Variable v = Variable.create(Type.INT, "v", "10");
        Variable w = Variable.create(Type.INT, "w", null);
        Call c = new Call(f, v, w);
        assertEquals(w, c.getDestination());
    }

    @Test(expected = TypeMismatch.class)
    public void testError() throws Exception {
        Function f = new Function("f", Type.INT, Type.INT);
        Variable v = Variable.create(Type.INT, "v", "10");
        Variable w = Variable.create(Type.BOOL, "w", null);
        Call c = new Call(f, v, w);
    }
}
