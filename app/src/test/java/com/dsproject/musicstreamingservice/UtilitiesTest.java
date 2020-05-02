package com.dsproject.musicstreamingservice;

import com.dsproject.musicstreamingservice.assist.Utilities;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class UtilitiesTest
{

    @Test
    public void toByteObjectArray()
    {
        byte[] inPrim = {3,1,-1,0,2};
        Byte[] inObj = {3,1,-1,0,2};

        byte[] inPrim1 = new byte[2];
        Byte[] inObj1 = new Byte[2];
        Arrays.fill(inObj1, (byte)0);

        assertArrayEquals(inObj, Utilities.toByteObjectArray(inPrim));
        assertArrayEquals(inObj1, Utilities.toByteObjectArray(inPrim1));
    }

    @Test
    public void toBytePrimitiveArray()
    {
        byte[] inPrim = {3,1,-1,0,2};
        Byte[] inObj = {3,1,-1,0,2};

        byte[] inPrim1 = new byte[2];
        Byte[] inObj1 = new Byte[2];
        Arrays.fill(inObj1, (byte)0);

        assertArrayEquals(inPrim, Utilities.toBytePrimitiveArray(inObj));
        assertArrayEquals(inPrim1, Utilities.toBytePrimitiveArray(inObj1));
    }

    @Test
    public void isNumeric()
    {
        assertTrue(Utilities.isNumeric("12"));
        assertTrue(Utilities.isNumeric("0"));
        assertTrue(Utilities.isNumeric(" 12 "));

        assertFalse(Utilities.isNumeric("vfd"));
        assertFalse(Utilities.isNumeric("f12"));
        assertFalse(Utilities.isNumeric("2r"));
        assertFalse(Utilities.isNumeric(" "));
        assertFalse(Utilities.isNumeric(""));
    }

    @Test
    public void isStringLiteral() {
        Object ob;

        ob = "string";
        assertTrue(Utilities.isStringLiteral(ob));

        ob = "2";
        assertTrue(Utilities.isStringLiteral(ob));

        ob = 2;
        assertFalse(Utilities.isStringLiteral(ob));
    }
}
