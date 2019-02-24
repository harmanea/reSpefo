package cz.cuni.mff.respefo.Listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;

/**
 * Example usage:<br/>
 * {@code
 * SelectionBoxListener boxListener = new SelectionBoxListener();
 * chart.getPlotArea().addMouseListener(boxListener);
 * chart.getPlotArea().addMouseMoveListener(boxListener);
 * chart.getPlotArea().addPaintListener(boxListener);}
 */
public class SelectionBoxListener implements MouseListener, MouseMoveListener, PaintListener {
	private boolean drag = false;
	private long startMillis;
	private int startX, startY;
	private int prevX, prevY;
	
	@Override
	public void mouseDoubleClick(MouseEvent event) {}

	@Override
	public void mouseDown(MouseEvent event) {
		startMillis = System.currentTimeMillis();
		
		if ((event.stateMask & SWT.CTRL) == SWT.CTRL) {
			drag = true;
			
			startX = event.x;
			startY = event.y;
			
			prevX = event.x;
			prevY = event.y;
		}
	}

	@Override
	public void mouseUp(MouseEvent event) {
		if (drag && System.currentTimeMillis() - startMillis > 50) { // drag release
			Chart chart = ReSpefo.getChart();
			Range chartXRange = chart.getAxisSet().getXAxis(0).getRange();
			Range chartYRange = chart.getAxisSet().getYAxis(0).getRange();
			
			Rectangle bounds = chart.getPlotArea().getBounds();
			
			Range xRange = new Range(chartXRange.lower + (chartXRange.upper - chartXRange.lower) * startX / bounds.width,
					chartXRange.lower + (chartXRange.upper - chartXRange.lower) * event.x / bounds.width);
			Range yRange = new Range(chartYRange.lower + (chartYRange.upper - chartYRange.lower) * (bounds.height - startY) / bounds.height,
					chartYRange.lower + (chartYRange.upper - chartYRange.lower) * (bounds.height - event.y) / bounds.height);
			
			for (IAxis xAxis : chart.getAxisSet().getXAxes()) {
				xAxis.setRange(xRange);
			}
			for (IAxis yAxis : chart.getAxisSet().getYAxes()) {
				yAxis.setRange(yRange);
			}
		
			chart.redraw();
		}
		
		drag = false;
	}

	@Override
	public void mouseMove(MouseEvent event) {
		if (drag) {
			prevX = event.x;
			prevY = event.y;
			ReSpefo.getChart().redraw();
		}
	}
	
	@Override
	public void paintControl(PaintEvent event) {
		if (drag) {
			event.gc.drawRectangle(startX, startY, prevX - startX, prevY - startY);
		}
	}
}
