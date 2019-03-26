package cz.cuni.mff.respefo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MathUtils {	
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
		return intep(x, y, xinter, null);
	}
	
	/**
	 * The INTEP interpolation algorithm
	 * 
	 * The INTEP interpolation algorithm is described by Hill 1982, PDAO 16, 67 ("Intep - an Effective Interpolation Subroutine"). This implementation is based on the FORTRAN code stated therein.
	 * 
	 * @param x Independent values sorted in strictly ascending order
	 * @param y Dependent values
	 * @param xinter Values at which to interpolate the tabulated data given by 'x' and 'y'
	 * @param fillValue This value will be used to represent values outside of the given bounds (null implies the last value in the bounds)
	 * @return Interpolated values at the locations specified by 'xinter'
	 */
	public static double[] intep(double[] x, double[] y, double[] xinter, Double fillValue) {
		// create result array
		double [] result = new double[xinter.length];
		
		// treat points outside of given bounds
		int ilow = 0; // lowest index of xinter value in the given bounds
		for (int i = 0; i < xinter.length; i++) {
			if (xinter[i] < x[0]) {
				ilow = i + 1;
				if (fillValue == null) {
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
				if (fillValue == null) {
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
			int infl = ArrayUtils.findFirstGreaterThen(x, xp);
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
	
	public static double robustMean(double[] x) {
		double t;
		int n = x.length;
		double[] dd = new double[n];
		
		int j, m1, m3, m1old, m3old;
		double s, ss, sc, z, xu, xl;
		
		Arrays.sort(x);
		
		m1 = n / 2;
		t = median(x);
		for (int i = 0; i < 5; i++) {
			for (int k = 0; k < n; k++) {
				dd[k] = Math.abs(x[k] - t);
			}
			
			Arrays.sort(dd);
			
			m1 = (n - 1) / 2;
			m3 = n / 2;
			
			s = 2.1 * median(dd);
			
			m1old = -1;
			m3old = -1;
			
			while (m1 != m1old && m3 != m3old) {
				xl = t - Math.PI * s;
				xu = t + Math.PI * s;
				
				j = 0;
				while (j < n && x[j] <= xl) {
					j++;
				}
				m1 = j - 1;
				
				j = n - 1;
				while (j >= 0 && x[j] >= xu) {
					j--;
				}
				m3 = j + 1;
				
				if (m1 != m1old || m3 != m3old) {
					m1old = m1;
					m3old = m3;
					
					ss = 0;
					sc = 0;
					for (j = m1 + 1; j <= m3 - 1; j++) {
						z = (x[j] - t) / s;
						ss += Math.sin(z);
						sc += Math.cos(z);
					}
					
					t += s * Math.atan(ss / sc);
				}
			}
			
		}
		
		return t;
	}
	
	/**
	 * @param values sorted list
	 * @return median of the values
	 */
	public static double median(double[] values) {
		if (values.length % 2 == 0)
		    return (values[values.length / 2] + values[values.length / 2 - 1]) / 2;
		else
		    return values[values.length / 2];
	}
	
	/**
	 * Calculate root mean square.
	 * @param values
	 * @return root mean square
	 */
	public static double rms(double[] values) {
		if (values.length <= 1) {
			throw new IllegalArgumentException("Array must contain at least two values.");
		}
		
		double sum = Arrays.stream(values).map(value -> Math.pow(value, 2)).sum();
		sum /= values.length;
		sum /= values.length - 1;
		
		return Math.sqrt(sum);
	}
	
	/**
	 * Calculate root mean square error
	 * @param values 
	 * @param actual predicted value
	 * @return root mean square error
	 */
	public static double rmse(double[] values, double actual) {
		if (values.length <= 1) {
			throw new IllegalArgumentException("Array must contain at least two values.");
		}
		
		double sum = Arrays.stream(values).map(value -> Math.pow(value - actual, 2)).sum();
		sum /= values.length;
		sum /= values.length - 1;
		
		return Math.sqrt(sum);
	}
	
	/**
	 * Rounds a double number
	 * @param number
	 * @param precision
	 * @return rounded number
	 */
	public static double round(double number, int precision) {
	    if (precision < 0) {
	    	throw new IllegalArgumentException("Precision must be a positive value.");
	    } else if (Double.isNaN(number) || Double.isInfinite(number)) {
	    	return number;
	    }

	    BigDecimal bigDecimal = new BigDecimal(number);
	    bigDecimal = bigDecimal.setScale(precision, RoundingMode.HALF_UP);
	    return bigDecimal.doubleValue();
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
	 * Formats a double to String and pads it with spaces from the left if necessary
	 * @param number to be formated
	 * @param before number of digits before decimal point
	 * @param after number of digits after decimal point
	 * @param sign include sign
	 * @return formatted String
	 */
	public static String formatDouble(double number, int before, int after, boolean sign) {
		String format = "%" + (sign ? " " : "") + before + "." + after + "f";
		return formatNumber(number, format, before + after + (sign ? 2 : 1));
	}
	
	/**
	 * Formats a double to String and pads it with spaces from the left if necessary, including sign
	 * @param number to be formated
	 * @param before number of digits before decimal point
	 * @param after number of digits after decimal point
	 * @return formatted String
	 */
	public static String formatDouble(double number, int before, int after) {
		return formatDouble(number, before, after, true);
	}
	
	/**
	 * Formats an integer to String and pads it with spaces from the left if necessary
	 * @param number to be formatted
	 * @param digits number of digits to show
	 * @param sign include sign
	 * @return formatted String
	 */
	public static String formatInteger(int number, int digits, boolean sign) {
		String format = "%" + (sign ? " " : "") + digits + "d";
		return formatNumber(number, format, digits + (sign ? 1 : 0));
	}
	
	/**
	 * Formats an integer to String and pads it with spaces from the left if necessary, including sign
	 * @param number to be formatted
	 * @param digits number of digits to show
	 * @return formatted String
	 */
	public static String formatInteger(int number, int digits) {
		return formatInteger(number, digits, true);
	}
	
	private static String formatNumber(Object number, String format, int targetLength) {
		String formattedNumber = String.format(format, number);
		
		for (int i = formattedNumber.length(); i <  targetLength; i++) {
			formattedNumber = ' ' + formattedNumber;
		}
		
		return formattedNumber;
	}
	
	/**
	 * Transform an index to wavelength using Taylor polynomials
	 * @param index
	 * @param coefficients array of polynomial coefficients
	 * @return wavelength
	 */
	public static double indexToLambda(double index, double[] coefficients) {
		double result = coefficients[5];
		for (int i = 4; i >= 0; --i) {
			result *= index;
			result += coefficients[i];
		}
		return result;
	}
}
