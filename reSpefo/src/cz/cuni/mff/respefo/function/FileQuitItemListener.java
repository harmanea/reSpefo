package cz.cuni.mff.respefo.function;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.ReSpefo;

public class FileQuitItemListener extends Function {
	private static FileQuitItemListener instance;
	
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());

	private FileQuitItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new FileQuitItemListener");
	}
	
	public static FileQuitItemListener getInstance() {
		if (instance == null) {
			instance = new FileQuitItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		ReSpefo.getShell().close();
	}

}
