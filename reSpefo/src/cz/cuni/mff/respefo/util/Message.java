package cz.cuni.mff.respefo.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import cz.cuni.mff.respefo.ReSpefo;

public class Message {
	private static final Logger logger = Logger.getLogger(Message.class.getName());
	
	public static void info(String message) {
		info(ReSpefo.getShell(), message);
	}
	
	public static void info(Shell shell, String message) {
		logger.log(Level.INFO, message);
		openMessageBox(shell, message, SWT.ICON_INFORMATION | SWT.OK);
	}
	
	public static void warning(String message) {
		warning(ReSpefo.getShell(), message);
	}
	
	public static void warning(Shell shell, String message) {
		logger.log(Level.WARNING, message);
		openMessageBox(shell, message, SWT.ICON_WARNING | SWT.OK);
	}
	
	public static void error(String message, Throwable thrown) {
		error(ReSpefo.getShell(), message, thrown);
	}
	
	public static void error(Shell shell, String message, Throwable thrown) {
		logger.log(Level.SEVERE, message, thrown);
		openMessageBox(shell, message + "\n\nError message:\n" + thrown.getMessage(), SWT.ICON_ERROR | SWT.OK);
	}
	
	public static int question(String message) {
		return question(ReSpefo.getShell(), message);
	}
	
	public static int question(Shell shell, String message) {
		return openMessageBox(shell, message, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
	}
	
	private static int openMessageBox(Shell shell, String message, int style) {
		MessageBox messageBox = new MessageBox(shell, style);
		
		messageBox.setMessage(message);
		return messageBox.open();
	}
}
