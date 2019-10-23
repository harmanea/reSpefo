package cz.cuni.mff.respefo.listeners;

import org.eclipse.swt.events.MouseEvent;

import cz.cuni.mff.respefo.ReSpefo;

public class CompareMouseDragListener extends MouseDragListener {
	
	public CompareMouseDragListener(boolean ignoreCtrl) {
		super(ignoreCtrl);
	}

	@Override
	public void mouseUp(MouseEvent event) {
		super.mouseUp(event);
		ReSpefo.getScene().forceFocus();
	}
}
