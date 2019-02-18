package cz.cuni.mff.respefo.measureRV;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

import cz.cuni.mff.respefo.Listeners.MouseWheelZoomListener;

public class MeasureRVMouseWheelZoomListener extends MouseWheelZoomListener {
	public MeasureRVMouseWheelZoomListener() {
		super();
	}
	
	public MeasureRVMouseWheelZoomListener(boolean zoomXCentred, boolean zoomYCentred) {
		super(zoomXCentred, zoomYCentred);
	}
	
	@Override
	public void mouseScrolled(MouseEvent e) {
		super.mouseScrolled(e);
		
		if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
			MeasureRVItemListener.getInstance().adjustRVStepLabel();
		}
	}
}
