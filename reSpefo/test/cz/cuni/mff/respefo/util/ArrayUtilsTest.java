package cz.cuni.mff.respefo.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ArrayUtilsTest {
	@Test
	public void testNDims() {
		Object data = new double[3][2][3];
		assertEquals(3, ArrayUtils.nDims(data));
		
		data = new double[5];
		assertEquals(1, ArrayUtils.nDims(data));
		
		data = "not an array";
		assertEquals(0, ArrayUtils.nDims(data));
	}
	
	@Test
	public void testValuesHaveSameDifference() {
		double[] values = {1, 3, 5, 7, 9};
		assertEquals(true, ArrayUtils.valuesHaveSameDifference(values));
		
		values = new double[]{1, 2};
		assertEquals(true, ArrayUtils.valuesHaveSameDifference(values));
		
		values = new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6};
		assertEquals(true, ArrayUtils.valuesHaveSameDifference(values));
		
		values = new double[]{1, 3, 6, 10};
		assertEquals(false, ArrayUtils.valuesHaveSameDifference(values));
	}
}
