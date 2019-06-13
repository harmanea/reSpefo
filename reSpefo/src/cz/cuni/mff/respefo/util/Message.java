package cz.cuni.mff.respefo.util;

import static java.util.logging.Level.*;
import static org.eclipse.swt.SWT.*;

import java.util.logging.Logger;

import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import cz.cuni.mff.respefo.ReSpefo;

public class Message {
	private static final Logger logger = Logger.getLogger(Message.class.getName());
	
	public static void fileSavedSuccessfuly() {
		info("File was successfully saved.");
	}
	
	public static void filesSavedSuccessfuly() {
		info("Files were successfully saved.");
	}
	
	public static void couldNotSaveFile() {
		warning("Couldn't save file.");
	}
	
	public static void couldNotSaveFile(Throwable thrown) {
		error("Couldn't save file.", thrown);
	}
	
	public static void info(String message) {
		info(ReSpefo.getShell(), message);
	}
	
	public static void info(Shell shell, String message) {
		logger.log(INFO, message);
		openMessageBox(shell, message, ICON_INFORMATION | OK);
	}
	
	public static void warning(String message) {
		warning(ReSpefo.getShell(), message);
	}
	
	public static void warning(Shell shell, String message) {
		logger.log(WARNING, message);
		openMessageBox(shell, message, ICON_WARNING | OK);
	}
	
	public static void error(String message, Throwable thrown) {
		error(ReSpefo.getShell(), message, thrown);
	}
	
	public static void error(Shell shell, String message, Throwable thrown) {
		logger.log(SEVERE, message, thrown);
		openMessageBox(shell, message + "\n\nError message:\n" + thrown.getMessage(), ICON_ERROR | OK);
	}
	
	public static boolean question(String message) {
		return question(ReSpefo.getShell(), message);
	}
	
	public static boolean question(Shell shell, String message) {
		return openMessageBox(shell, message, ICON_QUESTION | YES | NO) == YES;
	}
	
	private static int openMessageBox(Shell shell, String message, int style) {
		MessageBox messageBox = new MessageBox(shell, style);
		
		messageBox.setMessage(message);
		return messageBox.open();
	}
}
