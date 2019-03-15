package cz.cuni.mff.respefo.preparation;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.Listeners.AbstractSelectionListener;
import cz.cuni.mff.respefo.util.Message;

public class PrepareDirectoryItemListener extends AbstractSelectionListener {
	private static PrepareDirectoryItemListener instance;
	private PrepareDirectoryItemListener() {}
	
	public static PrepareDirectoryItemListener getInstance() {
		if (instance == null) {
			instance = new PrepareDirectoryItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		Message.info("This function is not available yet.");
	}
}
