package cz.cuni.mff.respefo;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxis.Position;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

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
	
	public static int findFirstGreaterThen(double[] array, double target) { // return array.length+1 if all values are smaller or equal than target
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
		for(int i = 0; i < array.length; i++) {
			array[i] *= BScale;
			array[i] += BZero;
		}
		return array;
	}
	
	public static double[] fillArray(int size, double from, double step) {
		double[] result = new double[size];
		for (int i = 0; i < size; i++) {
			result[i] = from + (i * step);
		}
		return result;
	}
	
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
	
	public static Spectrum importSpectrum() {
		Spectrum spectrum;
		
		FileDialog dialog = new FileDialog(ReSpefo.getShell(), SWT.OPEN);
		String[] filterNames = new String[] { "Spectrum Files", "All Files (*)" };
		String[] filterExtensions = new String[] { "*.txt;*.fits;*.fit;*.rui;*.uui", "*" };
		//String platform = SWT.getPlatform();

		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);

		String s = dialog.open();
		String extension;
		if (s == null) {
			return null;
		} else {
			int i = s.lastIndexOf('.');
			if (i < s.length()) {
				extension = s.substring(i + 1);
			} else {
				extension = "";
			}
		}
		
		MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
		
		switch (extension) {
		case "txt":
			spectrum = SpectrumBuilder.importFromASCIIFile(s);
			if (spectrum == null) {
				mb.setMessage("Couldn't import spectrum. File might be corrupt.");
				mb.open();
			}
			break;
		case "fits":
		case "fit":
			spectrum = SpectrumBuilder.importFromFitsFile(s);
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
