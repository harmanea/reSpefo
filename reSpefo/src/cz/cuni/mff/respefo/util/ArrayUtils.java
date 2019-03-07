package cz.cuni.mff.respefo.util;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ArrayUtils {
	/**
	 * Find first array entry that is greater than target
	 * @param array of values
	 * @param target
	 * @return index of the value matching the criteria, returns array.length if all values are smaller than or equal to the target
	 */
	public static int findFirstGreaterThen(double[] array, double target) {
        int index = Arrays.binarySearch(array, target);
        if (index >= 0) {
        	return index + 1;
        } else {
        	return -1 * (index + 1);
        }
	}
	
	/**
	 * Flips the array values
	 * @param array
	 * @return flipped array
	 */
	public static double[] mirrorArray(double[] array) {
		for(int i = 0; i < array.length / 2; i++)
		{
		    double temp = array[i];
		    array[i] = array[array.length - i - 1];
		    array[array.length - i - 1] = temp;
		}
		return array;
	}
	
	/**
	 * Add a value to all array entries
	 * @param array 
	 * @param value to add
	 * @return adjusted array
	 */
	public static double[] addValueToArrayElements(double[] array, double value) {
		return Arrays.stream(array).map(num -> num + value).toArray();
	}
	
	/**
	 * Divide all array entries by values in another array
	 * @param a1 array to be divided
	 * @param a2 array to divide with
	 * @return adjusted array
	 */
	public static double[] divideArrayValues(double[] a1, double[] a2) {
		if (a1.length != a2.length) {
			throw new IllegalArgumentException("Arrays must be of equal length");
		}
		
		return IntStream.range(0, a1.length).mapToDouble(i -> a1[i] / a2[i]).toArray();
	}
	
	/**
	 * Applies BScale to all array values
	 * @param array
	 * @param BZero
	 * @param BScale
	 * @return array with applied BScale
	 */
	public static double[] applyBScale(double[] array, double BZero, double BScale) {
		double[] result = new double[array.length];
		for(int i = 0; i < result.length; i++) {
			result[i] = array[i] * BScale;
			result[i] += BZero;
		}
		return result;
	}
	
	/**
	 * Creates a new array and fills it with values
	 * 
	 * @param size of the new array
	 * @param from first value in the array
	 * @param step difference between two values
	 * @return the created array
	 */
	public static double[] fillArray(int size, double from, double step) {
		double[] result = new double[size];
		for (int i = 0; i < size; i++) {
			result[i] = from + (i * step);
		}
		return result;
	}
	
	/**
	 * Returns the array values that are between the specified arguments.
	 * @param array to be trimmed
	 * @param from lower bound
	 * @param to upper bound
	 * @return trimmed array
	 */
	public static double[] trimArray(double[] array, double from, double to) {
		return trimArray(array, array, from, to);
	}
	
	/**
	 * Returns the array values of the points whose respective image values are between the specified arguments.
	 * @param array to be trimmed
	 * @param pattern values
	 * @param from lower bound
	 * @param to upper bounds
	 * @return trimmed array
	 */
	public static double[] trimArray(double[] array, double[] pattern, double from, double to) {
		if (array.length != pattern.length) {
			return null;
		}
		
		int lowerIndex = Arrays.binarySearch(pattern, from);
		int upperIndex = Arrays.binarySearch(pattern, to);
		
		if (lowerIndex < 0) {
			lowerIndex *= -1;
			lowerIndex -= 1;
		} else if (lowerIndex >= array.length) {
			lowerIndex = array.length - 1;
		}
		
		if (upperIndex < 0) {
			upperIndex *= -1;
			upperIndex -= 1;
		} else if (upperIndex >= array.length) {
			upperIndex = array.length - 1;
		}
		
		return Arrays.copyOfRange(array, lowerIndex, upperIndex);
	}
}
