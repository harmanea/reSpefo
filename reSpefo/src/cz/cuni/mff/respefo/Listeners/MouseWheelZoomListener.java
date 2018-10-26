package cz.cuni.mff.respefo.Listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;

/**
 * Adds zooming capabilities.
 * Hold ctrl and scroll mouse wheel to zoom in or out.
 */
public class MouseWheelZoomListener implements MouseWheelListener {
	private boolean zoomXCentred;
	private boolean zoomYCentred;
	
	public MouseWheelZoomListener() {
		this(false, false);
	}
	
	public MouseWheelZoomListener(boolean zoomXCentred, boolean zoomYCentred) {
		this.zoomXCentred = zoomXCentred;
		this.zoomYCentred = zoomYCentred;
	}
	
	@Override
	public void mouseScrolled(MouseEvent e) {
		if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
			Chart chart = ReSpefo.getChart();
			
			if (e.count > 0) {
				if (zoomXCentred && zoomYCentred) {
					chart.getAxisSet().zoomIn();
				} else {
					Rectangle bounds = chart.getPlotArea().getBounds();
					if (zoomXCentred) {
						for (IAxis axis : ReSpefo.getChart().getAxisSet().getXAxes()) {
							axis.zoomIn();
						}
					} else {
						Range chartXRange = chart.getAxisSet().getXAxis(0).getRange();
						double realX = chartXRange.lower + ((chartXRange.upper - chartXRange.lower) * ((double)e.x / bounds.width));
						
						for (IAxis axis : ReSpefo.getChart().getAxisSet().getXAxes()) {
							axis.zoomIn(realX);
						}
					}
	
					if (zoomYCentred) {
						for (IAxis axis : ReSpefo.getChart().getAxisSet().getYAxes()) {
							axis.zoomIn();
						}
					} else {
						Range chartYRange = chart.getAxisSet().getYAxis(0).getRange();
						double realY = chartYRange.upper - ((chartYRange.upper - chartYRange.lower) * ((double)e.y / bounds.height));
						
						for (IAxis axis : ReSpefo.getChart().getAxisSet().getYAxes()) {
							axis.zoomIn(realY);
						}
					}
				}
				
				
			} else if (e.count < 0) {
				chart.getAxisSet().zoomOut();
			}
			
			chart.redraw();
		}
	}

}
