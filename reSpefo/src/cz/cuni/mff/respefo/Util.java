package cz.cuni.mff.respefo;

import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

public class Util {
	
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
	
	public static int findFirstGreaterThen(double[] array, double target) { // returns array.length if all values are smaller or equal than target
        int index = Arrays.binarySearch(array, target);
        if (index >= 0) {
        	return index + 1;
        } else {
        	return -1 * (index + 1);
        }
	}
	
	public static double[] mirrorArray(double[] array) {
		for(int i = 0; i < array.length / 2; i++)
		{
		    double temp = array[i];
		    array[i] = array[array.length - i - 1];
		    array[array.length - i - 1] = temp;
		}
		return array;
	}
	
	public static double[] adjustArrayValues(double[] array, double value) {
		for(int i = 0; i < array.length; i++) {
			array[i] += value;
		}
		return array;
	}
	
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
	 * 
	 * @param input array of floats to be converted to doubles
	 * @return array of doubles
	 */
	public static double[] convertFloatsToDoubles(float[] input)
	{
	    if (input == null)
	    {
	        return null;
	    }
	    double[] output = new double[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = input[i];
	    }
	    return output;
	}
	
	/**
	 * 
	 * @param input array of ints to be converted to doubles
	 * @return array of doubles
	 */
	public static double[] convertIntsToDoubles(int[] input)
	{
	    if (input == null)
	    {
	        return null;
	    }
	    double[] output = new double[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = input[i];
	    }
	    return output;
	}
	
	/**
	 * 
	 * @param input array of shorts to be converted to doubles
	 * @return array of doubles
	 */
	public static double[] convertShortsToDoubles(short[] input)
	{
	    if (input == null)
	    {
	        return null;
	    }
	    double[] output = new double[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = input[i];
	    }
	    return output;
	}
	
	/**
	 * 
	 * @param input array of longs to be converted to doubles
	 * @return array of doubles
	 */
	public static double[] convertLongsToDoubles(long[] input)
	{
	    if (input == null)
	    {
	        return null;
	    }
	    double[] output = new double[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = input[i];
	    }
	    return output;
	}
	
	/**
	 * 
	 * @param input array of bytes to be converted to doubles
	 * @return array of doubles
	 */
	public static double[] convertBytesToDoubles(byte[] input)
	{
	    if (input == null)
	    {
	        return null;
	    }
	    double[] output = new double[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = input[i];
	    }
	    return output;
	}
	
	public static void clearListeners() {
		for (Listener l : ReSpefo.getShell().getListeners(SWT.KeyDown)) {
			ReSpefo.getShell().removeListener(SWT.KeyDown, l);
		}
		
		for (Listener l : ReSpefo.getShell().getListeners(SWT.MouseDown)) {
			ReSpefo.getShell().removeListener(SWT.MouseDown, l);
		}
	}
	
	public static final int Spectrum = 0;
	public static final int Stl = 1;
	
	public static String openFileDialog(int type) {
		FileDialog dialog = new FileDialog(ReSpefo.getShell(), SWT.OPEN);
		dialog.setText("Choose file");
		dialog.setFilterPath(ReSpefo.getFilterPath());
			
		String[] filterNames;
		String[] filterExtensions;
		
		switch (type) {
		case Spectrum:
			filterNames = new String[] { "Spectrum Files", "All Files (*)" };
			filterExtensions = new String[] { "*.fits;*.fit;*.fts;*.txt, *.ascii;*.rui;*.uui", "*" };
			break;
		case Stl:
			filterNames = new String[] { "Stl Files", "All Files (*)" };
			filterExtensions = new String[] { "*.stl", "*" };
			break;
		default:
			return null;
		}

		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);

		String s = dialog.open();
		
		if (s != null && Paths.get(s).getParent() != null) {
			ReSpefo.setFilterPath(Paths.get(s).getParent().toString());
		}
		
		return s;
	}
	
	public static Spectrum importSpectrum() {
		return importSpectrum(Util.openFileDialog(Spectrum));
	}
	
	public static Spectrum importSpectrum(String name) {
		Spectrum spectrum;
		
		String extension;
		if (name == null) {
			return null;
		} else {
			int i = name.lastIndexOf('.');
			if (i < name.length()) {
				extension = name.substring(i + 1);
			} else {
				extension = "";
			}
		}
		
		MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
		
		switch (extension) {
		case "":
		case "txt":
			spectrum = SpectrumBuilder.importFromASCIIFile(name);
			if (spectrum == null) {
				mb.setMessage("Couldn't import spectrum. File might be corrupt.");
				mb.open();
			}
			break;
		case "fits":
		case "fit":
		case "fts":
			spectrum = SpectrumBuilder.importFromFitsFile(name);
			if (spectrum == null) {
				mb.setMessage("Couldn't import spectrum. File might be corrupt.");
				mb.open();
			}
			break;
		case "rui":
		case "uui":
			mb.setMessage("Old Spefo formats aren't supported yet.");
			mb.open();
			spectrum = null;
			break;
		default:
			mb.setMessage("Not a supported file type.");
			mb.open();
			spectrum = null;
			break;
		}
		
		return spectrum;
	}
	
	private Util() {}
}
