package cz.cuni.mff.respefo.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Message {
	private static final Logger logger = Logger.getLogger(Message.class.getName());
	
	public static void info(Shell shell, String message) {
		logger.log(Level.INFO, message);
		openMessageBox(shell, message, SWT.ICON_INFORMATION | SWT.OK);
	}
	
	public static void warning(Shell shell, String message) {
		logger.log(Level.WARNING, message);
		openMessageBox(shell, message, SWT.ICON_WARNING | SWT.OK);
	}
	
	public static void error(Shell shell, String message, Throwable thrown) {
		logger.log(Level.SEVERE, message, thrown);
		openMessageBox(shell, message + "\n\nError message:\n" + thrown.getMessage(), SWT.ICON_ERROR | SWT.OK);
	}
	
	private static void openMessageBox(Shell shell, String message, int style) {
		MessageBox messageBox = new MessageBox(shell, style);
		
		messageBox.setMessage(message);
		messageBox.open();
	}
}
