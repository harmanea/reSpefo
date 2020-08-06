package cz.cuni.mff.respefo.listeners;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.function.MeasureEWItemListener;

public class MeasureEWMouseDragListener implements MouseListener, MouseMoveListener {
	private boolean drag = false;
	private int prevX;
	
	@Override
	public void mouseMove(MouseEvent event) {
		if (drag) {
			Range xRange = ReSpefo.getChart().getAxisSet().getXAxis(0).getRange();
			
			double shift = ((event.x - prevX) * (xRange.upper - xRange.lower)) / ReSpefo.getChart().getPlotArea().getBounds().width;
			
			MeasureEWItemListener.getInstance().updateShift(shift);
			
			prevX = event.x;
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent event) {}

	@Override
	public void mouseDown(MouseEvent event) {
		drag = true;
		
		prevX = event.x;
		
		MeasureEWItemListener.getInstance().startMoving();
	}

	@Override
	public void mouseUp(MouseEvent event) {
		drag = false;
		
		MeasureEWItemListener.getInstance().stopMoving();
	}
}
