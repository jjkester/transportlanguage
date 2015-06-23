package otld.otld.intermediate;

import org.junit.Test;
import otld.otld.intermediate.exceptions.TypeMismatch;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ApplicationTest {

    @Test
    public void testGetOperator() throws Exception {
        Variable v = Variable.create(Type.INT, "v", "10");
        Variable w = Variable.create(Type.INT, "w", "5");
        Application a = new Application(Operator.ADDITION, v, w, v);
        assertEquals(Operator.ADDITION, a.getOperator());
    }

    @Test
    public void testGetArgs() throws Exception {
        Variable v = Variable.create(Type.INT, "v", "10");
        Variable w = Variable.create(Type.INT, "w", "5");
        Application a = new Application(Operator.ADDITION, v, w, v);
        Variable[] args = {v, w};
        assertArrayEquals(args, a.getArgs());
    }

    @Test
    public void testGetVariable() throws Exception {
        Variable v = Variable.create(Type.INT, "v", "10");
        Variable w = Variable.create(Type.INT, "w", "5");
        Application a = new Application(Operator.ADDITION, v, w, v);
        assertEquals(v, a.getVariable());
    }

    @Test(expected = TypeMismatch.class)
    public void testError() throws Exception {
        Variable v = Variable.create(Type.INT, "v", "10");
        Variable w = Variable.create(Type.BOOL, "w", "false");
        Application a = new Application(Operator.ADDITION, v, w, v);
    }
}