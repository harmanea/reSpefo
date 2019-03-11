package cz.cuni.mff.respefo.preparation;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import cz.cuni.mff.respefo.util.Message;

public class PrepareDirectoryItemListener implements SelectionListener {
	private static PrepareDirectoryItemListener instance;
	private PrepareDirectoryItemListener() {}
	
	public static PrepareDirectoryItemListener getInstance() {
		if (instance == null) {
			instance = new PrepareDirectoryItemListener();
		}
		
		return instance;
	}

	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		handle(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		handle(event);
	}

	private void handle(SelectionEvent event) {
		Message.info("This function is not available yet.");
	}
}
