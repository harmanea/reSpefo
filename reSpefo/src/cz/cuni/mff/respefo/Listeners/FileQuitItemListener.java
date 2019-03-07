package cz.cuni.mff.respefo.Listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.util.Message;

public class FileQuitItemListener implements SelectionListener {
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
	
	@Override
	public void widgetSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "File quit widget selected");
		handle(event);
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "File quit default widget selected");
		handle(event);
	}

	private void handle(SelectionEvent event) {
		if (Message.question("Are you sure you want to quit?") == SWT.YES) {
			ReSpefo.getShell().dispose();
		}
	}

}
