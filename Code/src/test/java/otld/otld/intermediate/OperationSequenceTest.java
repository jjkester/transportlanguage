package otld.otld.intermediate;

import org.junit.Test;
import otld.otld.intermediate.exceptions.TypeMismatch;

import static org.junit.Assert.*;

public class OperationSequenceTest {
    Function add = new Function("add", Type.INT, Type.INT, Type.INT);
    Function eq = new Function("eq", Type.INT, Type.INT, Type.BOOL);
    Variable x = Variable.create(Type.INT, "x", null);
    Variable y = Variable.create(Type.INT, "y", null);
    Variable z = Variable.create(Type.BOOL, "z", null);

    @Test
    public void testBehaviour() throws TypeMismatch {
        OperationSequence sequence = new OperationSequence();

        ValueAssignment op1 = x.createValueAssignment(10);
        sequence.add(op1);
        ValueAssignment op2 = x.createValueAssignment(20);
        sequence.add(op2);
        Call op3 = new Call(add, x, y, y);
        sequence.add(op3);
        ValueAssignment op4 = x.createValueAssignment(30);
        sequence.add(op4);
        Call op5 = new Call(eq, x, y, z);
        sequence.add(op5);

        assertEquals(5, sequence.size());

        assertEquals(op1, sequence.get(0));
        assertEquals(op2, sequence.get(1));
        assertEquals(op3, sequence.get(2));
        assertEquals(op4, sequence.get(3));
        assertEquals(op5, sequence.get(4));
    }

}