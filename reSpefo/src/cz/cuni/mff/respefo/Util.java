package cz.cuni.mff.respefo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

public class Util {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	/**
	 * The INTEP interpolation algorithm
	 * 
	 * The INTEP interpolation algorithm is described by Hill 1982, PDAO 16, 67 ("Intep - an Effective Interpolation Subroutine"). This implementation is based on the FORTRAN code stated therein.
	 * 
	 * @param x Independent values sorted in strictly ascending order
	 * @param y Dependent values
	 * @param xinter Values at which to interpolate the tabulated data given by 'x' and 'y'
	 * @return Interpolated values at the locations specified by 'xinter'
	 */
	public static double[] intep(double[] x, double[] y, double[] xinter) {
		return intep(x, y, xinter, Double.NaN);
	}
	
	/**
	 * The INTEP interpolation algorithm
	 * 
	 * The INTEP interpolation algorithm is described by Hill 1982, PDAO 16, 67 ("Intep - an Effective Interpolation Subroutine"). This implementation is based on the FORTRAN code stated therein.
	 * 
	 * @param x Independent values sorted in strictly ascending order
	 * @param y Dependent values
	 * @param xinter Values at which to interpolate the tabulated data given by 'x' and 'y'
	 * @param fillValue This value will be used to represent values outside of the given bounds (Double.NaN implies the last value in the bounds)
	 * @return Interpolated values at the locations specified by 'xinter'
	 */
	public static double[] intep(double[] x, double[] y, double[] xinter, double fillValue) {
		// create result array
		double [] result = new double[xinter.length];
		
		// treat points outside of given bounds
		int ilow = 0; // lowest index of xinter value in the given bounds
		for (int i = 0; i < xinter.length; i++) {
			if (xinter[i] < x[0]) {
				ilow = i + 1;
				if (Double.isNaN(fillValue)) {
					result[i] = y[0];
				} else {
					result[i] = fillValue;
				}
			} else {
				break;
			}
		}
		int iup = xinter.length - 1; // highest index of xinter value in the given bounds
		for (int i = xinter.length - 1; i >= 0; i--) {
			if (xinter[i] > x[x.length - 1]) {
				iup = i - 1;
				if (Double.isNaN(fillValue)) {
					result[i] = y[y.length - 1];
				} else {
					result[i] = fillValue;
				}
			} else {
				break;
			}
		}
		
		// treat points inside bounds
		double xp, xpi, xpi1, l1, l2, lp1, lp2, fp1, fp2;
		
		for (int i = ilow; i <= iup; i++) {
			xp = xinter[i];
			int infl = findFirstGreaterThen(x, xp);
			if (infl == x.length) {
				result[i] = y[y.length - 1];
				continue;
			}
			infl--;
			lp1 = 1 / (x[infl] - x[infl + 1]);
			lp2 = -lp1;
			
			if (infl <= 0) {
				// first point
				fp1 = (y[1] - y[0]) / (x[1] - x[0]);
			} else {
				fp1 = (y[infl + 1] - y[infl - 1]) / (x[infl + 1] - x[infl - 1]);
			}
			
			if (infl >= x.length - 2) {
				// last point
				fp2 = (y[y.length - 1] - y[y.length - 2]) / (x[x.length - 1] - x[x.length - 2]);
			} else {
				fp2 = (y[infl + 2] - y[infl]) / (x[infl + 2] - x[infl]);
			}
			
			xpi1 = xp - x[infl + 1];
			xpi = xp - x[infl];
			l1 = xpi1 * lp1;
			l2 = xpi * lp2;
			
			result[i] = y[infl] * (1 - 2 * lp1 * xpi) * l1 * l1 + y[infl + 1] * (1 - 2 * lp2 * xpi1) * l2 * l2
					+ fp2 * xpi1 * l2 * l2 + fp1 * xpi * l1 * l1;
		}
		
		
		return result;
	}
	
	/**
	 * Calculate root mean square.
	 * @param values
	 * @return root mean square
	 */
	public static double rms(double[] values) {
		if (values.length == 0) {
			return Double.NaN;
		}
		
		double sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += Math.pow(values[i], 2);
		}
		
		sum /= values.length;
		
		return Math.sqrt(sum);
	}
	
	/**
	 * Calculate root mean square error
	 * @param values 
	 * @param middle predicted value
	 * @return root mean square error
	 */
	public static double rms(double[] values, double middle) {
		if (values.length == 0) {
			return Double.NaN;
		}
		
		double sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += Math.pow(values[i] - middle, 2);
		}
		
		sum /= values.length;
		
		return Math.sqrt(sum);
	}
	
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
	public static double[] adjustArrayValues(double[] array, double value) {
		for(int i = 0; i < array.length; i++) {
			array[i] += value;
		}
		return array;
	}
	
	/**
	 * Divide all array entries by values in another array
	 * @param a1 array to be divided
	 * @param a2 array to divide with
	 * @return adjusted array
	 */
	public static double[] divideArrayValues(double[] a1, double[] a2) {
		if (a1.length != a2.length) {
			return null;
		}
		double[] result = new double[a1.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = a1[i] / a2[i];
		}
		return result;
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
	
	/**
	 * Removes all listeners registered on the main shell.
	 */
	public static void clearShellListeners() {
		int[] eventTypes = { 3007, 3011, SWT.Resize, SWT.Move, SWT.Dispose,
	            SWT.DragDetect, 3000, SWT.FocusIn, SWT.FocusOut, SWT.Gesture,
	            SWT.Help, SWT.KeyUp, SWT.KeyDown, 3001, 3002, SWT.MenuDetect,
	            SWT.Modify, SWT.MouseDown, SWT.MouseUp, SWT.MouseDoubleClick,
	            SWT.MouseMove, SWT.MouseEnter, SWT.MouseExit, SWT.MouseHover,
	            SWT.MouseWheel, SWT.Paint, 3008, SWT.Selection, SWT.Touch,
	            SWT.Traverse, 3005, SWT.Verify, 3009, 3010 };

	    for (int eventType : eventTypes) {
	        Listener[] listeners = ReSpefo.getShell().getListeners(eventType);
	        for (Listener listener : listeners) {
	        	LOGGER.log(Level.FINER, "Removing listener " + eventType + " " + listener);
	        	ReSpefo.getShell().removeListener(eventType, listener);
	        }
	    }
	}
	
	/**
	 * Remove all listeners registered on the control.
	 * @param control to clear listeners from
	 */
	public static void clearControlListeners(Control control) {
		int[] eventTypes = { 3007, 3011, SWT.Resize, SWT.Move, SWT.Dispose,
	            SWT.DragDetect, 3000, SWT.FocusIn, SWT.FocusOut, SWT.Gesture,
	            SWT.Help, SWT.KeyUp, SWT.KeyDown, 3001, 3002, SWT.MenuDetect,
	            SWT.Modify, SWT.MouseDown, SWT.MouseUp, SWT.MouseDoubleClick,
	            SWT.MouseMove, SWT.MouseEnter, SWT.MouseExit, SWT.MouseHover,
	            SWT.MouseWheel, SWT.Paint, 3008, SWT.Selection, SWT.Touch,
	            SWT.Traverse, 3005, SWT.Verify, 3009, 3010 };

	    for (int eventType : eventTypes) {
	        Listener[] listeners = control.getListeners(eventType);
	        for (Listener listener : listeners) {
	        	LOGGER.log(Level.FINER, "Removing listener " + eventType + " " + listener);
	        	ReSpefo.getShell().removeListener(eventType, listener);
	        }
	    }
	}
	
	public static final int SPECTRUM_LOAD = 0;
	public static final int SPECTRUM_SAVE = 1;
	public static final int STL_LOAD = 2;
	public static final int LST_LOAD = 3;
	
	/**
	 * Opens a file dialog and returns it's result
	 * @param type
	 * @return dialog return value
	 */
	public static String openFileDialog(int type) {
		FileDialog dialog;
		
		String text;
		String[] filterNames;
		String[] filterExtensions;
		
		switch (type) {
		case SPECTRUM_LOAD:
			dialog = new FileDialog(ReSpefo.getShell(), SWT.OPEN);
			text = "Import file";
			filterNames = new String[] { "Spectrum Files", "All Files (*)" };
			filterExtensions = new String[] { "*.fits;*.fit;*.fts;*.txt;*.ascii;*.rui;*.uui;*.rci;*.rfi", "*" };
			break;
		case SPECTRUM_SAVE:
			dialog = new FileDialog(ReSpefo.getShell(), SWT.SAVE);
			if (ReSpefo.getSpectrum() != null) {
				dialog.setFileName(ReSpefo.getSpectrum().getName());
			}
			text = "Export file";
			filterNames = new String[] { "Spectrum Files", "All Files (*)" };
			filterExtensions = new String[] { "*.fits;*.fit;*.fts;*.txt;*.ascii;*.rui;*.uui;*.rci;*.rfi", "*" };
			break;
		case STL_LOAD:
			dialog = new FileDialog(ReSpefo.getShell(), SWT.OPEN);
			text = "Open file";
			filterNames = new String[] { "Stl Files", "All Files (*)" };
			filterExtensions = new String[] { "*.stl", "*" };
			break;
		case LST_LOAD:
			dialog = new FileDialog(ReSpefo.getShell(), SWT.OPEN);
			text = "Open file";
			filterNames = new String[] { "Lst Files", "All Files (*)" };
			filterExtensions = new String[] { "*.lst", "*" };
			break;
		default:
			return null;
		}

		dialog.setText(text);
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);

		dialog.setFilterPath(ReSpefo.getFilterPath());
		
		String s = dialog.open();
		
		if (s != null && Paths.get(s).getParent() != null) {
			ReSpefo.setFilterPath(Paths.get(s).getParent().toString());
		}
		
		return s;
	}
	
	/**
	 * Gets the file extension
	 * @param fileName
	 * @return file extension
	 */
	public static String getFileExtension(String fileName) {
		if (fileName == null) {
			return null;
		} else {
			int i = fileName.lastIndexOf('.');
			if (i >= 0) {
				return fileName.substring(i + 1);
			} else {
				return "";
			}
		}
	}
	
	/**
	 * Converts a Turbo Pascal Extended type to double
	 * @param data byte array containing the extended value in little endian byte order
	 * @return converted double value
	 */
	public static double pascalExtendedToDouble(byte[] data) {
		if (data.length != 10) {
			throw new IllegalArgumentException("Extended format takes up 10 bytes");
		}
		
		byte sign = (byte) ((data[9] & 0x80) >> 7); // 0 positive, 1 negative
		
		byte[] bytes = Arrays.copyOfRange(data, 8, 10);
		short exponent = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
		exponent -= 16382; // to get the actual value;
		
		double result;
		if (exponent >= -1024) {
			double mantissa = 0;
			
			for (int i = 0; i < 8; i++) {
				mantissa += (int) (data[i] & 0xFF);
				mantissa /= 256;
			}
			
			result = Math.pow(-1, sign) * mantissa * Math.pow(2, exponent);
		} else {
			result = 0;
		}
		
		return result;
	}
	
	/**
	 * Converts a Turbo Pascal Real type to double
	 * @param data byte array containing the real value in little endian byte order
	 * @return converted double value
	 */
	public static double pascalRealToDouble(byte[] data) {
		if (data.length != 6) {
			throw new IllegalArgumentException("Real format takes up 6 bytes");
		}
		
		byte sign = (byte) ((data[5] & 0x80) >> 7); // 0 positive, 1 negative
		
		int exponent = (byte) (data[0] & 0xFF);
		exponent -= 129;
		
		double mantissa = 0;
		for (int i = 1; i < 5; i++) {
			mantissa += (int) (data[i] & 0xFF);
			mantissa /= 256;
		}
		
		mantissa += (int) (data[5] & 0x7F);
		mantissa /= 128;
		mantissa += 1;
		
		if ((byte) (data[0] & 0xFF) == 0 && mantissa == 1) {
			return 0;
		}
		
		return Math.pow(-1, sign) * mantissa * Math.pow(2, exponent);
	}
	
	/**
	 * A very dumb implementation of rounding double values, use with caution
	 * @param num
	 * @param precision
	 * @return rounded number
	 */
	public static double round(double num, int precision) {
		double scale = Math.pow(10, precision);
		return Math.round(num * scale) / scale;
	}
	
	/**
	 * Increments the last number in a file name
	 * <br/>
	 * For example: fileName001.txt --> filename002.txt
	 * @param fileName
	 * @return incremented file name
	 */
	public static String incrementFileName(String fileName) {
		String fileExtension = getFileExtension(fileName);
		if (fileExtension.length() > 0) {
			fileName = fileName.substring(0, fileName.length() - fileExtension.length());
		}
		
		Pattern pattern = Pattern.compile("[0-9]");
		Matcher matcher = pattern.matcher(fileName);
		
		int start = 0, end = 0;
		String group = null;
		while (matcher.find()) {
			start = matcher.start();
			end = matcher.end();
			group = matcher.group();
		}
		
		if (group == null) {
			return null;
		}
		
		int number = Integer.parseInt(group);
		number += 1;
		
		group = Integer.toString(number);
		for (int i = group.length(); i < end - start; i++) {
			group = '0' + group;
		}
		
		fileName = fileName.substring(0, start) + group + fileName.substring(end);
		
		return fileName + fileExtension;
		
	}
	
	/**
	 * Formats a double to String and pads it with spaces if necessary
	 * @param num to be formated
	 * @param before number of digits before decimal point
	 * @param after number of digits after decimal point
	 * @param sign include sign
	 * @return formated String
	 */
	public static String formatDouble(double num, int before, int after, boolean sign) {
		String format = "%" + (sign ? " " : "") + before + "." + after + "f";
		String s = String.format(format, num);
		
		for (int i = s.length(); i <  before + after + (sign ? 2 : 1); i++) {
			s = ' ' + s;
		}
		
		return s;
	}
	
	/**
	 * Formats a double to String and pads it with spaces if necessary, including sign
	 * @param num to be formated
	 * @param before number of digits before decimal point
	 * @param after number of digits after decimal point
	 * @return formated String
	 */
	public static String formatDouble(double num, int before, int after) {
		return formatDouble(num, before, after, true);
	}
	
	private Util() {}
}
