package cz.cuni.mff.respefo;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;
import org.swtchart.Range;

/**
 * Class for easier creation of charts
 *
 */
public class ChartBuilder {
	public static final Color BLACK = new Color(Display.getDefault(), 0, 0, 0);
	public static final Color BLUE = new Color(Display.getDefault(), 0, 0, 255);
	public static final Color CYAN = new Color(Display.getDefault(), 0, 255, 255);
	public static final Color GREEN = new Color(Display.getDefault(), 0, 255, 0);
	public static final Color GRAY = new Color(Display.getDefault(), 128, 128, 128);
	public static final Color ORANGE = new Color(Display.getDefault(), 255, 128, 0);
	public static final Color PINK = new Color(Display.getDefault(), 255, 0, 255);
	public static final Color PURPLE = new Color(Display.getDefault(), 128, 0, 255);
	public static final Color RED = new Color(Display.getDefault(), 255, 0, 0);
	public static final Color YELLOW = new Color(Display.getDefault(), 255, 255, 0);
	public static final Color WHITE = new Color(Display.getDefault(), 255, 255, 255);

	private static Color primaryColor = YELLOW;
	private static Color secondaryColor = BLACK;

	/**
	 * Returns the primary color.
	 * 
	 * @return the primary color
	 */
	public static Color getPrimaryColor() {
		return primaryColor;
	}

	/**
	 * Sets the primary color.
	 * 
	 * @param c
	 *            the primary color to set
	 */
	public static void setPrimaryColor(Color c) {
		primaryColor = c;
	}

	/**
	 * Returns the secondary color.
	 * 
	 * @return the secondary color
	 */
	public static Color getSecondaryColor() {
		return secondaryColor;
	}

	/**
	 * Sets the secondary color.
	 * 
	 * @param c
	 *            the secondary color to set
	 */
	public static void setSecondaryColor(Color c) {
		secondaryColor = c;
	}

	private Chart chart;
	private ArrayList<ILineSeries> series;

	private void setTheme() {
		chart.getTitle().setForeground(primaryColor);

		chart.setBackground(secondaryColor);
		chart.setBackgroundInPlotArea(secondaryColor);

		IAxisSet axisset = chart.getAxisSet();

		axisset.getXAxis(0).getGrid().setForeground(secondaryColor);
		axisset.getYAxis(0).getGrid().setForeground(secondaryColor);

		axisset.getXAxis(0).getTick().setForeground(primaryColor);
		axisset.getYAxis(0).getTick().setForeground(primaryColor);
		axisset.getXAxis(0).getTitle().setForeground(primaryColor);
		axisset.getYAxis(0).getTitle().setForeground(primaryColor);

		chart.getLegend().setVisible(false);
	}

	/**
	 * Instantiates new chart with the shell as parent
	 * 
	 * @param shell
	 *            to be set as chart's parent
	 */
	public ChartBuilder(Shell shell) {
		chart = new Chart(shell, SWT.NONE);
		series = new ArrayList<>();
	}

	/**
	 * Instantiates new chart with the composite as parent
	 * 
	 * @param parent
	 *            to be set as chart's parent
	 */
	public ChartBuilder(Composite parent) {
		chart = new Chart(parent, SWT.NONE);
		series = new ArrayList<>();
	}

	/**
	 * Sets the chart's title
	 * 
	 * @param title
	 *            to be set as the chart's title
	 * @return the adjusted ChartBuilder
	 */
	public ChartBuilder setTitle(String title) {
		chart.getTitle().setText(title);
		return this;
	}

	/**
	 * Sets the chart's XAxis label
	 * 
	 * @param label
	 *            to be set as the chart's XAxis label
	 * @return the adjusted ChartBuilder
	 */
	public ChartBuilder setXAxisLabel(String label) {
		chart.getAxisSet().getXAxis(0).getTitle().setText(label);
		return this;
	}

	/**
	 * Sets the chart's YAxis label
	 * 
	 * @param label
	 *            to be set as the chart's YAxis label
	 * @return the adjusted ChartBuilder
	 */
	public ChartBuilder setYAxisLabel(String label) {
		chart.getAxisSet().getYAxis(0).getTitle().setText(label);
		return this;
	}

	/**
	 * Adds a new line series to the chart
	 * 
	 * @param style
	 *            of the added series
	 * @param name
	 *            of the added series
	 * @param color
	 *            of the added series
	 * @param XSeries
	 *            of the added series
	 * @param YSeries
	 *            of the added series
	 * @return the adjusted ChartBuilder
	 */
	public ChartBuilder addLineSeries(LineStyle style, String name, Color color, double[] XSeries, double[] YSeries) {
		return addLineSeries(style, 1, name, color, XSeries, YSeries);
	}
	
	/**
	 * Adds a new line series to the chart
	 * 
	 * @param style
	 *            of the added series
	 * @param lineWidth
	 *            of the added series
	 * @param name
	 *            of the added series
	 * @param color
	 *            of the added series
	 * @param XSeries
	 *            of the added series
	 * @param YSeries
	 *            of the added series
	 * @return the adjusted ChartBuilder
	 */
	public ChartBuilder addLineSeries(LineStyle style, int lineWidth, String name, Color color, double[] XSeries, double[] YSeries) {
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, name);
		lineSeries.setXSeries(XSeries);
		lineSeries.setYSeries(YSeries);

		lineSeries.setLineStyle(style);
		lineSeries.setSymbolType(PlotSymbolType.NONE);
		lineSeries.setLineColor(color);
		lineSeries.setLineWidth(lineWidth);

		addSeries(lineSeries);

		return this;
	}
	
	/**
	 * Adds a new scatter series to the chart
	 * 
	 * @param style
	 *            of the added series
	 * @param name
	 *            of the added series
	 * @param color
	 *            of the added series
	 * @param xSeries
	 *            of the added series
	 * @param ySeries
	 *            of the added series
	 * @return the adjusted ChartBuilder
	 */
	public ChartBuilder addScatterSeries(PlotSymbolType style, String name, Color color, double[] xSeries, double[] ySeries) {
		return addScatterSeries(style, 3, name, color, xSeries, ySeries);
	}

	/**
	 * Adds a new scatter series to the chart
	 * 
	 * @param style
	 *            of the added series
	 * @param symbolSize
	 *            of the added series
	 * @param name
	 *            of the added series
	 * @param color
	 *            of the added series
	 * @param xSeries
	 *            of the added series
	 * @param ySeries
	 *            of the added series
	 * @return the adjusted ChartBuilder
	 */
	public ChartBuilder addScatterSeries(PlotSymbolType style, int symbolSize, String name, Color color, double[] xSeries, double[] ySeries) {
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, name);
		lineSeries.setXSeries(xSeries);
		lineSeries.setYSeries(ySeries);

		lineSeries.setLineStyle(LineStyle.NONE);
		lineSeries.setSymbolType(PlotSymbolType.CIRCLE);
		lineSeries.setSymbolColor(color);
		lineSeries.setSymbolSize(symbolSize);

		addSeries(lineSeries);

		return this;
	}
	
	private void addSeries(ILineSeries lineSeries) {
		if (series.size() > 0) {
			int YaxisId = chart.getAxisSet().createYAxis();
			IAxis yAxis = chart.getAxisSet().getYAxis(YaxisId);

			yAxis.getTick().setVisible(false);
			yAxis.getTitle().setVisible(false);
			yAxis.getGrid().setForeground(secondaryColor);

			lineSeries.setYAxisId(YaxisId);

			int XaxisId = chart.getAxisSet().createXAxis();
			IAxis xAxis = chart.getAxisSet().getXAxis(XaxisId);

			xAxis.getTick().setVisible(false);
			xAxis.getTitle().setVisible(false);
			xAxis.getGrid().setForeground(secondaryColor);

			lineSeries.setXAxisId(XaxisId);
		}

		series.add(lineSeries);
	}

	/**
	 * Adjusts the view so that all series are fully visible
	 * 
	 * @return the adjusted ChartBuilder
	 */
	public ChartBuilder adjustRange() {
		adjustRange(chart);
		return this;
	}

	/**
	 * Adjusts the view so that one series is fully visible
	 * 
	 * @param index
	 *            of the series to be centered (as order of insertion)
	 * @return the adjusted ChartBuilder
	 */
	public ChartBuilder adjustRange(int index) {
		int XAxisId = series.get(index).getXAxisId();
		int YAxisId = series.get(index).getXAxisId();
		adjustRange(chart, XAxisId, YAxisId);
		return this;
	}

	/**
	 * Returns the created chart
	 * 
	 * @return the created Chart
	 */
	public Chart build() {
		setTheme();
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		chart.getParent().layout();
		return chart;
	}

	/**
	 * Adjusts the chart range so that all series are fully visible
	 * 
	 * @param chart
	 *            chart to be adjusted
	 */
	public static void adjustRange(Chart chart) {
		adjustRange(chart, 0, 0);
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
