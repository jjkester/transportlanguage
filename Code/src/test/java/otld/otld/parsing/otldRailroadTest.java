package otld.otld.parsing;

import org.junit.Test;
import otld.otld.intermediate.Type;

import static org.junit.Assert.*;

/**
 * Unit tests for otldRailroad visitor
 */
public class otldRailroadTest {

    @Test
    public void testGetType() throws Exception {
        /** Test railroad */
        otldRailroad rail = new otldRailroad();

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
        otldRailroad rail = new otldRailroad();

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


}