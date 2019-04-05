package cz.cuni.mff.respefo.function;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.dialog.PrepareDirectoryDialog;
import cz.cuni.mff.respefo.util.Message;

public class PrepareDirectoryItemListener extends Function {
	private static PrepareDirectoryItemListener instance;
	private PrepareDirectoryItemListener() {}
	
	public static PrepareDirectoryItemListener getInstance() {
		if (instance == null) {
			instance = new PrepareDirectoryItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		new PrepareDirectoryDialog(ReSpefo.getShell()).open();
		Message.info("This function is not available yet.");
	}
}
