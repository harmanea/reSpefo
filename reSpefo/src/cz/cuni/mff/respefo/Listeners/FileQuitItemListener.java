package cz.cuni.mff.respefo.Listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;

import cz.cuni.mff.respefo.ReSpefo;

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
		MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
		mb.setMessage("Are you sure you want to quit?");
		if (mb.open() == SWT.YES) {
			LOGGER.log(Level.FINER, "Quit dialog confirmed");
			ReSpefo.getShell().dispose();
		} else {
			LOGGER.log(Level.FINER, "Quit dialog cancelled");
		}
	}

}
