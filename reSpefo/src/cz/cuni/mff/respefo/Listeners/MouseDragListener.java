package cz.cuni.mff.respefo.Listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;

/**
 * Example usage:<br/>
 * {@code
 * MouseDragListner dragListener = new MouseDragListener(true);
 * chart.getPlotArea().addMouseListener(dragListener);
 * chart.getPlotArea().addMouseMoveListener(dragListener);}
 */
public class MouseDragListener implements MouseListener, MouseMoveListener {
	private boolean drag = false;
	private int prevX, prevY;
	private boolean ignoreCtrl;
	
	public MouseDragListener() {
		this(false);
	}
	
	public MouseDragListener(boolean ignoreCtrl) {
		this.ignoreCtrl = ignoreCtrl;
	}

	@Override
	public void mouseMove(MouseEvent e) {	
		if (drag) {
			Chart chart = ReSpefo.getChart();
			Range chartXRange = chart.getAxisSet().getXAxis(0).getRange();
			Range chartYRange = chart.getAxisSet().getYAxis(0).getRange();
			
			Rectangle bounds = chart.getPlotArea().getBounds();
			
			double xIncrease = (double)(prevX - e.x) / bounds.width;
			Range xRange = new Range(chartXRange.lower + (chartXRange.upper - chartXRange.lower) * xIncrease,
					chartXRange.upper + (chartXRange.upper - chartXRange.lower) * xIncrease);
			
			double yIncrease = (double)(e.y - prevY) / bounds.height; 
			Range yRange = new Range(chartYRange.lower + (chartYRange.upper - chartYRange.lower) * yIncrease,
					chartYRange.upper + (chartYRange.upper - chartYRange.lower) * yIncrease);
			
			for (IAxis x : chart.getAxisSet().getXAxes()) {
				x.setRange(xRange);
			}
			for (IAxis y : chart.getAxisSet().getYAxes()) {
				y.setRange(yRange);
			}
		
			chart.redraw();
	
			prevX = e.x;
			prevY = e.y;
		}
	}
	
	@Override
	public void mouseDoubleClick(MouseEvent e) {}

	@Override
	public void mouseDown(MouseEvent e) {
		if (!ignoreCtrl || ((e.stateMask & SWT.CTRL) != SWT.CTRL)) {
			drag = true;
			
			prevX = e.x;
			prevY = e.y;
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
		drag = false;
	}

}
