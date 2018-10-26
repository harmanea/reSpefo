package cz.cuni.mff.respefo;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

import cz.cuni.mff.respefo.legacy.OldSpectrum;
import cz.cuni.mff.respefo.legacy.SpectrumBuilder;

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
	
	@Deprecated
	public static OldSpectrum importSpectrum() {
		return importSpectrum(Util.openFileDialog(SPECTRUM_LOAD));
	}
	
	@Deprecated
	public static OldSpectrum importSpectrum(String name) {
		OldSpectrum spectrum;
		
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
			/*
			spectrum = SpectrumBuilder.importFromFitsFile(name);
			if (spectrum == null) {
				mb.setMessage("Couldn't import spectrum. File might be corrupt.");
				mb.open();
			}
			break;
			*/
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
	
	public static String getFileExtension(String fileName) {
		if (fileName == null) {
			return null;
		} else {
			int i = fileName.lastIndexOf('.');
			if (i < fileName.length()) {
				return fileName.substring(i + 1);
			} else {
				return "";
			}
		}
	}
	
	private Util() {}
}
