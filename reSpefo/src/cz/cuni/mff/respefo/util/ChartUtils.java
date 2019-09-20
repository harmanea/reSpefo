package cz.cuni.mff.respefo.util;

import java.util.Arrays;

import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.Range;

public class ChartUtils {
	/**
	 * Adjusts the chart range so that all series are fully visible
	 * 
	 * @param chart
	 *            chart to be adjusted
	 */
	public static void adjustRange(Chart chart) {
		double xMax = Double.NaN, xMin = Double.NaN, yMax = Double.NaN, yMin = Double.NaN;
		
		for (ISeries series : chart.getSeriesSet().getSeries()) {
			double[] xSeries = series.getXSeries();
			
			if (xSeries.length == 0) {
				continue;
			}
			
			if (Double.isNaN(xMax) || xMax < xSeries[xSeries.length - 1]) {
				xMax = xSeries[xSeries.length - 1];
			}
			if (Double.isNaN(xMin) || xMin > xSeries[0]) {
				xMin = xSeries[0];
			}
			
			double[] ySeries = series.getYSeries();
			double ySeriesMax = Arrays.stream(ySeries).max().getAsDouble();
			double ySeriesMin = Arrays.stream(ySeries).min().getAsDouble();
			
			if (Double.isNaN(yMax) || yMax < ySeriesMax) {
				yMax = ySeriesMax;
			}
			if (Double.isNaN(yMin) || yMin > ySeriesMin) {
				yMin = ySeriesMin;
			}
			
		}
		
		Range xRange = new Range(xMin, xMax);
		Range yRange = new Range(yMin, yMax);
		
		for (IAxis xAxis : chart.getAxisSet().getXAxes()) {
			xAxis.setRange(xRange);
		}
		for (IAxis yAxis : chart.getAxisSet().getYAxes()) {
			yAxis.setRange(yRange);
		}
	}

	/**
	 * Adjusts the chart so that one of the series is fully visible
	 * 
	 * @param chart
	 *            chart to be adjusted
	 * @param XAxisId
	 *            to be adjusted around
	 * @param YAxisId
	 *            to be adjusted around
	 */
	public static void adjustRange(Chart chart, int XAxisId, int YAxisId) {
		chart.getAxisSet().adjustRange();
		if (chart.getAxisSet().getAxes().length > 2) {
			Range XRange = chart.getAxisSet().getXAxis(XAxisId).getRange();
			Range YRange = chart.getAxisSet().getYAxis(YAxisId).getRange();
			for (IAxis xAxis : chart.getAxisSet().getXAxes()) {
				if (xAxis.getId() != XAxisId) {
					xAxis.setRange(XRange);
				}
			}
			for (IAxis yAxis : chart.getAxisSet().getYAxes()) {
				if (yAxis.getId() != YAxisId) {
					yAxis.setRange(YRange);
				}
			}
		}
	}
}
