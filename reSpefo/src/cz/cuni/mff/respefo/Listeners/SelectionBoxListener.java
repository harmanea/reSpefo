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
	public void mouseDoubleClick(MouseEvent e) {}

	@Override
	public void mouseDown(MouseEvent e) {
		startMillis = System.currentTimeMillis();
		
		if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
			drag = true;
			
			startX = e.x;
			startY = e.y;
			
			prevX = e.x;
			prevY = e.y;
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
		if (drag && System.currentTimeMillis() - startMillis > 50) { // drag release
			Chart chart = ReSpefo.getChart();
			Range chartXRange = chart.getAxisSet().getXAxis(0).getRange();
			Range chartYRange = chart.getAxisSet().getYAxis(0).getRange();
			
			Rectangle bounds = chart.getPlotArea().getBounds();
			
			Range xRange = new Range(chartXRange.lower + (chartXRange.upper - chartXRange.lower) * startX / bounds.width,
					chartXRange.lower + (chartXRange.upper - chartXRange.lower) * e.x / bounds.width);
			Range yRange = new Range(chartYRange.lower + (chartYRange.upper - chartYRange.lower) * (bounds.height - startY) / bounds.height,
					chartYRange.lower + (chartYRange.upper - chartYRange.lower) * (bounds.height - e.y) / bounds.height);
			
			for (IAxis x : chart.getAxisSet().getXAxes()) {
				x.setRange(xRange);
			}
			for (IAxis y : chart.getAxisSet().getYAxes()) {
				y.setRange(yRange);
			}
		
			chart.redraw();
		}
		
		drag = false;
	}

	@Override
	public void mouseMove(MouseEvent e) {
		if (drag) {
			prevX = e.x;
			prevY = e.y;
			ReSpefo.getChart().redraw();
		}
	}
	
	@Override
	public void paintControl(PaintEvent e) {
		if (drag) {
			e.gc.drawRectangle(startX, startY, prevX - startX, prevY - startY);
		}
	}
}
