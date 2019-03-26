package cz.cuni.mff.respefo.listeners;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public abstract class AbstractSelectionListener implements SelectionListener {	
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
