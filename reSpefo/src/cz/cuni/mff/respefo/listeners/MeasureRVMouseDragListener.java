package cz.cuni.mff.respefo.listeners;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.function.MeasureRVItemListener;

public class MeasureRVMouseDragListener implements MouseListener, MouseMoveListener {
	private boolean drag = false;
	private int prevX;
	
	@Override
	public void mouseMove(MouseEvent event) {
		if (drag) {
			Chart chart = ReSpefo.getChart();
			
			ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
			Range xRange = chart.getAxisSet().getXAxis(series.getXAxisId()).getRange();
			
			double shift = ((event.x - prevX) * (xRange.upper - xRange.lower)) / ReSpefo.getChart().getPlotArea().getBounds().width;
			
			MeasureRVItemListener.getInstance().move(shift);
			
			prevX = event.x;
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent event) {}

	@Override
	public void mouseDown(MouseEvent event) {
		drag = true;
		
		prevX = event.x;
	}

	@Override
	public void mouseUp(MouseEvent event) {
		drag = false;
	}

}
