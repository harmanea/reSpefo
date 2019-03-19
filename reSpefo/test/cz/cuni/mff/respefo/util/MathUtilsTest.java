package cz.cuni.mff.respefo.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class MathUtilsTest {

	@Test
	public void testRound() {
		double number = 12345.12345;
		
		assertEquals(12345.123, MathUtils.round(number, 3), 0);
		assertEquals(12345.12345, MathUtils.round(number, 8), 0);
		assertEquals(12345, MathUtils.round(number, 0), 0);
		assertEquals(12345.12345, MathUtils.round(number, 5), 0);
		
		number = -12345.12345;
		
		assertEquals(-12345.123, MathUtils.round(number, 3), 0);
		assertEquals(-12345.12345, MathUtils.round(number, 8), 0);
		assertEquals(-12345, MathUtils.round(number, 0), 0);
		assertEquals(-12345.12345, MathUtils.round(number, 5), 0);
	}
	
	@Test
	public void testFormatDouble() {
		double number = 12345.12345;
		
		assertEquals("    12345.12345000", MathUtils.formatDouble(number, 8, 8));
		assertEquals(" 12345.123", MathUtils.formatDouble(number, 5, 3));
		assertEquals("12345.1235", MathUtils.formatDouble(number, 1, 4, false));
		assertEquals("12345", MathUtils.formatDouble(number, 1, 0, false));
		
		number = -123.123;
		
		assertEquals("-123.123", MathUtils.formatDouble(number, 3, 3));
		assertEquals("   -123.12", MathUtils.formatDouble(number, 6, 2));
		assertEquals("-123.1230", MathUtils.formatDouble(number, 1, 4, false));
		assertEquals("-123", MathUtils.formatDouble(number, 1, 0, false));
		
		assertEquals(MathUtils.formatDouble(number, 3, 3), MathUtils.formatDouble(number, 3, 3, true));
	}

	@Test
	public void testFormatInteger() {
		int number = 12345;
		
		assertEquals("    12345", MathUtils.formatInteger(number, 8));
		assertEquals("12345", MathUtils.formatInteger(number, 4, false));
		assertEquals(" 12345", MathUtils.formatInteger(number, 4, true));
		assertEquals("12345", MathUtils.formatInteger(number, 1, false));
		
		number = -123;
		
		assertEquals("     -123", MathUtils.formatInteger(number, 8));
		assertEquals("    -123", MathUtils.formatInteger(number, 8, false));
		assertEquals(" -123", MathUtils.formatInteger(number, 4, true));
		assertEquals("-123", MathUtils.formatInteger(number, 1, false));
		
		assertEquals(MathUtils.formatInteger(number, 8), MathUtils.formatInteger(number, 8, true));
	}
}
