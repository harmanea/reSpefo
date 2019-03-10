package cz.cuni.mff.respefo.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

import cz.cuni.mff.respefo.ReSpefo;

@Deprecated
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
	
	private Util() {}
}
