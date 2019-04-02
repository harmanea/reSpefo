package cz.cuni.mff.respefo.listeners;

import java.util.logging.Logger;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import cz.cuni.mff.respefo.ReSpefo;

public abstract class AbstractSelectionListener implements SelectionListener {	
	protected static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		handle(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		handle(event);
	}

	public abstract void handle(SelectionEvent event);
}
