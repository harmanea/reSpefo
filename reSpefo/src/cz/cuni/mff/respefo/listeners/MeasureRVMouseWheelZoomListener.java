package cz.cuni.mff.respefo.listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

import cz.cuni.mff.respefo.function.MeasureRVItemListener;

public class MeasureRVMouseWheelZoomListener extends MouseWheelZoomListener {
	public MeasureRVMouseWheelZoomListener() {
		super();
	}
	
	public MeasureRVMouseWheelZoomListener(boolean zoomXCentred, boolean zoomYCentred) {
		super(zoomXCentred, zoomYCentred);
	}
	
	@Override
	public void mouseScrolled(MouseEvent event) {
		super.mouseScrolled(event);
		
		if ((event.stateMask & SWT.CTRL) == SWT.CTRL) {
			MeasureRVItemListener.getInstance().adjustRVStepLabel();
		}
	}
}
