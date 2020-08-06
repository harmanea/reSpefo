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
	
	public static int findClosest(double[] array, double target) {
		int index = Arrays.binarySearch(array, target);
		if (index >= 0) {
			return index;
			
		} else {
			int insertionPoint = -1 * (index + 1);
			if (insertionPoint == 0) {
				return insertionPoint;
			} else if (insertionPoint == array.length) {
				return insertionPoint - 1;
			} else {
				double lowDiff = target - array[insertionPoint - 1];
				double topDiff = array[insertionPoint] - target;
				
				return lowDiff > topDiff ? insertionPoint : insertionPoint - 1;
			}
		}
	}
	
	/**
	 * Flips the array values
	 * @param array
	 * @return new array with flipped values
	 */
	public static double[] mirrorArray(double[] array) {
		double[] newArray = array.clone();
		for(int i = 0; i < newArray.length / 2; i++)
		{
		    double temp = newArray[i];
		    newArray[i] = newArray[newArray.length - i - 1];
		    newArray[newArray.length - i - 1] = temp;
		}
		return newArray;
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
	 * Multiply all array entries with a value
	 * @param array 
	 * @param value to add
	 * @return adjusted array
	 */
	public static double[] multiplyArrayElements(double[] array, double value) {
		return Arrays.stream(array).map(num -> num * value).toArray();
	}
	
	/**
	 * Divide all array entries by values in another array
	 * @param numerators array to be divided
	 * @param denominators array to divide with
	 * @return adjusted array
	 */
	public static double[] divideArrayValues(double[] numerators, double[] denominators) {
		if (numerators.length != denominators.length) {
			throw new IllegalArgumentException("Arrays must be of equal length");
		}
		
		return IntStream.range(0, numerators.length).mapToDouble(i -> numerators[i] / denominators[i]).toArray();
	}
	
    /**
     * Divide all array entries by a given value
     * @param numerators array to be divided
     * @param denominator value to divide with
     * @return adjusted array
     */
    public static double[] divideArrayValues(double[] numerators, double denominator) {
        return IntStream.range(0, numerators.length).mapToDouble(i -> numerators[i] / denominator).toArray();
    }
	
	/**
	 * Applies BScale to all array values
	 * @param array
	 * @param bZero
	 * @param bScale
	 * @return array with applied BScale
	 */
	public static double[] applyBScale(double[] array, double bZero, double bScale) {
		return IntStream.range(0, array.length).mapToDouble(i -> array[i]*bScale + bZero).toArray();
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
		return IntStream.range(0, size).mapToDouble(i -> from + i*step).toArray();
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
	
	public static int nDims(Object data) {
		return 1 + data.getClass().getName().lastIndexOf('[');
	}
	
	public static boolean valuesHaveSameDifference(double[] values) {
		if (values.length < 2) {
			throw new IllegalArgumentException("Values must contain at least two values");
		} else if (values.length == 2) {
			return true;
		}
		
		return IntStream.range(2, values.length - 1)
				.allMatch(i -> MathUtils.doublesEqual(values[i] - values[i - 1], values[i - 1] - values[i - 2]));
	}
}
