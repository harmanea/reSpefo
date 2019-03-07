package cz.cuni.mff.respefo.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import cz.cuni.mff.respefo.ReSpefo;

public class Util {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
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
	public static final int RVR_LOAD = 4;
	
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
		case RVR_LOAD:
			dialog = new FileDialog(ReSpefo.getShell(), SWT.OPEN);
			text = "Open file";
			filterNames = new String[] { "Rvr Files", "All Files (*)" };
			filterExtensions = new String[] { "*.rvr", "*" };
			break;
		default:
			return null;
		}

		dialog.setText(text);
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);

		dialog.setFilterPath(ReSpefo.getFilterPath());
		
		String fileName = dialog.open();
		
		if (fileName != null && Paths.get(fileName).getParent() != null) {
			ReSpefo.setFilterPath(Paths.get(fileName).getParent().toString());
		}
		
		return fileName;
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
			int index = fileName.lastIndexOf('.');
			if (index >= 0) {
				return fileName.substring(index + 1);
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
		
		Pattern pattern = Pattern.compile("[0-9]+");
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
		String formattedNumber = String.format(format, num);
		
		for (int i = formattedNumber.length(); i <  before + after + (sign ? 2 : 1); i++) {
			formattedNumber = ' ' + formattedNumber;
		}
		
		return formattedNumber;
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
