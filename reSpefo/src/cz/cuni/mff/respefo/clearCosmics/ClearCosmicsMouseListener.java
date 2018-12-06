package cz.cuni.mff.respefo.clearCosmics;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;

public class ClearCosmicsMouseListener implements MouseMoveListener {

	@Override
	public void mouseMove(MouseEvent e) {
Chart chart = ReSpefo.getChart();
		
		ILineSeries series = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries("points");
		
		double[] xSeries = series.getXSeries();
		double[] ySeries = series.getYSeries();
		
		Rectangle bounds = chart.getPlotArea().getBounds();
		
		Range yRange = chart.getAxisSet().getYAxis(0).getRange();
		Range xRange = chart.getAxisSet().getXAxis(0).getRange();
		
		int index = -1;
		int closest = Integer.MAX_VALUE;
		
		for (int i = 0; i < xSeries.length; i++) {
			if (xSeries[i] >= xRange.lower && xSeries[i] <= xRange.upper && ySeries[i] >= yRange.lower && ySeries[i] <= yRange.upper) {
				int x = (int) ((xSeries[i] - xRange.lower) / (xRange.upper - xRange.lower) * bounds.width);
				int y = (int) ((1 - (ySeries[i] - yRange.lower) / (yRange.upper - yRange.lower)) * bounds.height);
				
				int distance = (int) Math.sqrt(Math.pow(x - e.x, 2) + Math.pow(y - e.y, 2));
				
				if (distance < closest) {
					index = i;
					closest = distance;
				}
			}
		}
		
		if (index != -1) {
			ClearCosmicsItemListener.getInstance().selectPoint(index);
		}
	}

}
