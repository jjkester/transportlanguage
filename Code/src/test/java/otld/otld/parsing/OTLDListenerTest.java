package otld.otld.parsing;

import org.junit.Test;
import otld.otld.Main;
import otld.otld.intermediate.*;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Unit tests for OTLDListener visitor
 */
public class OTLDListenerTest {

    @Test
    public void testGetType() throws Exception {
        /** Test railroad */
        OTLDListener rail = new OTLDListener();

        /** Correct input */
        String bool = "boolean";
        String integer = "int";
        String character = "char";
        /** Invalid input */
        String err1 = "Boolean";
        String err2 = "bool";
        String err3 = "integer";
        String err4 = "booleaN";
        String err5 = "character";

        assertEquals(Type.BOOL, rail.getType(bool));
        assertEquals(Type.CHAR, rail.getType(character));
        assertEquals(Type.INT, rail.getType(integer));

        assertNull(rail.getType(err1));
        assertNull(rail.getType(err2));
        assertNull(rail.getType(err3));
        assertNull(rail.getType(err4));
        assertNull(rail.getType(err5));
    }

    @Test
    public void testGetArrType() throws Exception {
        /** Test railroad */
        OTLDListener rail = new OTLDListener();

        /** Correct input */
        String bool = "boolean";
        String integer = "int";
        String character = "char";
        /** Invalid input */
        String err1 = "Boolean";
        String err2 = "bool";
        String err3 = "integer";
        String err4 = "booleaN";
        String err5 = "character";

        assertEquals(Type.BOOLARR, rail.getArrType(bool));
        assertEquals(Type.CHARARR, rail.getArrType(character));
        assertEquals(Type.INTARR, rail.getArrType(integer));

        assertNull(rail.getArrType(err1));
        assertNull(rail.getArrType(err2));
        assertNull(rail.getArrType(err3));
        assertNull(rail.getArrType(err4));
        assertNull(rail.getArrType(err5));
    }


    @Test
    /**
     * Test the working of a valid otld program.
     */
    public void testProgram1() throws Exception {
        InputStream stream = new FileInputStream(OTLDListenerTest.class.getResource("Enschede.tldr").getPath());
        OTLDListener railroad = OTLDListener.parseFile(stream);
        Program city = railroad.getProgram();

        // If any errors occur Main should print them to the standard error output
        Main.handleCompileErrors(railroad.getErrors());

        assertEquals(true, railroad.getErrors().isEmpty());
        assertEquals("Enschede", city.getId());

        assertEquals(Type.INT,  city.getVariable("a").getType());
        assertEquals(Type.BOOL, city.getVariable("b").getType());
        assertEquals(Type.CHAR, city.getVariable("c").getType());
        assertEquals(Type.INT,  city.getVariable("d").getType());

        assertEquals(Type.BOOL, city.getFunction("notLessThanOrEquals").getType());
        assertEquals(2, city.getFunction("notLessThanOrEquals").getArgTypes().length);

        // Validate if all the operations indeed occur in the order that we expect them
        assertTrue(city.getBody().getFirst() instanceof ValueAssignment);
        assertTrue(city.getBody().get(1) instanceof ValueAssignment);
        assertTrue(city.getBody().get(2) instanceof ValueAssignment);

        assertTrue(city.getBody().get(3) instanceof Call);
        assertTrue(city.getBody().get(4) instanceof Call);
        assertTrue(city.getBody().get(5) instanceof Loop);

        assertTrue(city.getBody().get(6) instanceof Output);
        assertTrue(city.getBody().get(7) instanceof Output);
        assertTrue(city.getBody().get(8) instanceof Output);
        assertTrue(city.getBody().get(9) instanceof Output);
        assertTrue(city.getBody().get(10) instanceof Output);
        assertTrue(city.getBody().get(11) instanceof Output);
        assertTrue(city.getBody().get(12) instanceof Output);

        // Validate the contents of the loop operation
        assertTrue(((Loop) city.getBody().get(5)).getConditionBody().getFirst() instanceof Output);
        assertTrue(((Loop) city.getBody().get(5)).getBody().get(0) instanceof Application);
        assertTrue(((Loop) city.getBody().get(5)).getBody().get(1) instanceof Call);

        // Validate ValueAssignment operation
        assertEquals(25, ((ValueAssignment) city.getBody().getFirst()).getValue());
        assertEquals(1, ((ValueAssignment) city.getBody().get(1)).getValue());
        assertEquals('c', ((ValueAssignment) city.getBody().get(2)).getValue());
    }

    @Test
    /**
     * Testing of an invalid otld program.
     * We are expecting a variable already declared error and
     * a type mismatch error.
     */
    public void testProgram2() throws Exception {
        InputStream stream = new FileInputStream(OTLDListenerTest.class.getResource("Amsterdam.tldr").getPath());
        OTLDListener railroad = OTLDListener.parseFile(stream);
        Program city = railroad.getProgram();

        // Verify that we indeed encounter errors
        assertEquals(false, railroad.getErrors().isEmpty());
        assertEquals(2, railroad.getErrors().size());
        assertEquals("Error at line:6:10: This variable has already been defined!",railroad.getErrors().get(0).getError());
        assertEquals("Error at line:12:19: These types do not match!",railroad.getErrors().get(1).getError());

    }

    @Test
    /**
     * Testing of an invalid otld program.
     * We are expecting a variable not declared, a factory already declared error and
     * a type mismatch error.
     */
    public void testProgram3() throws Exception {
        InputStream stream = new FileInputStream(OTLDListenerTest.class.getResource("Almere.tldr").getPath());
        OTLDListener railroad = OTLDListener.parseFile(stream);
        Program city = railroad.getProgram();

        // Verify that we indeed encounter errors
        assertEquals(false, railroad.getErrors().isEmpty());
        assertEquals(7, railroad.getErrors().size());

        assertEquals("Error at line:17:12: This factory has already been defined!",railroad.getErrors().get(0).getError());
        assertEquals("Error at line:20:22: This variable has not been defined!",railroad.getErrors().get(1).getError());
        assertEquals("Error at line:20:33: This variable has not been defined!",railroad.getErrors().get(2).getError());
        assertEquals("Error at line:20:77: This variable has not been defined!",railroad.getErrors().get(3).getError());
        assertEquals("Error at line:20:22: These types do not match!",railroad.getErrors().get(4).getError());
        assertEquals("Error at line:22:26: This variable has not been defined!",railroad.getErrors().get(5).getError());
        assertEquals("Error at line:27:26: This variable has not been defined!",railroad.getErrors().get(6).getError());
    }

    @Test
    /**
     * The test will return a syntax error, a function not defined error should not be given
     * since syntax errors will stop the walking of the tree
     */
    public void testProgram4() throws Exception {
        InputStream stream = new FileInputStream(OTLDListenerTest.class.getResource("Maastricht.tldr").getPath());
        OTLDListener railroad = OTLDListener.parseFile(stream);
        Program city = railroad.getProgram();

        // Verify that we indeed encounter errors
        assertEquals(false, railroad.getErrors().isEmpty());
        assertEquals(1, railroad.getErrors().size());
        assertEquals("Error at line:6:20: Syntax error on:mismatched input 'bool' expecting CARGO)",railroad.getErrors().get(0).getError());
    }

    @Test
    /**
     * We expect a reserved name and factory undefined error
     */
    public void testProgram5() throws Exception {
        InputStream stream = new FileInputStream(OTLDListenerTest.class.getResource("Nijmegen.tldr").getPath());
        OTLDListener railroad = OTLDListener.parseFile(stream);
        Program city = railroad.getProgram();

        // Verify that we indeed encounter errors
        assertEquals(false, railroad.getErrors().isEmpty());
        assertEquals(2, railroad.getErrors().size());
        assertEquals("Error at line:6:10: The ID name is a reserved name and cannot be used!",railroad.getErrors().get(0).getError());
        assertEquals("Error at line:10:27: This factory has not been defined!",railroad.getErrors().get(1).getError());
    }
}
