package cz.cuni.mff.respefo.measureRV;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;

public class MeasureRVMouseDragListener implements MouseListener, MouseMoveListener {
	private boolean drag = false;
	private int prevX;
	
	@Override
	public void mouseMove(MouseEvent e) {
		if (drag) {
			Chart chart = ReSpefo.getChart();
			
			ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
			Range xRange = chart.getAxisSet().getXAxis(series.getXAxisId()).getRange();
			
			double shift = ((e.x - prevX) * (xRange.upper - xRange.lower)) / ReSpefo.getChart().getPlotArea().getBounds().width;
			
			MeasureRVItemListener.getInstance().move(shift);
			
			prevX = e.x;
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {}

	@Override
	public void mouseDown(MouseEvent e) {
		drag = true;
		
		prevX = e.x;
	}

	@Override
	public void mouseUp(MouseEvent e) {
		drag = false;
	}

}
