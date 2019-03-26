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

	@Test
	public void testRobustMean() {
		double[] values = { 29.3936, 27.9973, 25.5745, 23.6468, 29.7703, 29.4919, 28.4588, 27.6771, 24.1238, 28.1947,
				30.3977, 26.3970, 27.2327, 23.0827, 33.0418, 31.5371, 34.0490 };
		double expected = 28.2193;

		double actual = MathUtils.robustMean(values);

		assertEquals(expected, actual, 0.0001);

		values = new double[] { 28.392, 23.949, 28.848, 22.65, 27.622, 27.937, 27.5, 29.081, 26.116, 25.472, 32.387,
				27.795, 24.517, 25.654, 30.337, 29.419, 30.763, 23.517 };
		expected = 27.345;

		actual = MathUtils.robustMean(values);

		assertEquals(expected, actual, 0.001);

		values = new double[] { -23.233, -0.378 };
		expected = -11.805;

		actual = MathUtils.robustMean(values);

		assertEquals(expected, actual, 0.001);
	}

	@Test
	public void testRmse() {
		double[] values = { 29.3936, 27.9973, 25.5745, 23.6468, 29.7703, 29.4919, 28.4588, 27.6771, 24.1238, 28.1947,
				30.3977, 26.3970, 27.2327, 23.0827, 33.0418, 31.5371, 34.0490 };
		double predicted = 28.2193;
		double mean = 0.7528;

		double actual = MathUtils.rmse(values, predicted);

		assertEquals(mean, actual, 0.0001);

		values = new double[] { 28.392, 23.949, 28.848, 22.65, 27.622, 27.937, 27.5, 29.081, 26.116, 25.472, 32.387,
				27.795, 24.517, 25.654, 30.337, 29.419, 30.763, 23.517 };
		predicted = 27.345;
		mean = 0.63;

		actual = MathUtils.rmse(values, predicted);

		assertEquals(mean, actual, 0.001);

		values = new double[] { -23.233, -0.378 };
		predicted = -11.805;
		mean = 11.428;

		actual = MathUtils.rmse(values, predicted);

		assertEquals(mean, actual, 0.001);
	}
}
