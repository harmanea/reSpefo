package cz.cuni.mff.respefo;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.Range;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

public class ChartBuilder {
	public static final Color black = new Color(Display.getDefault(), 0, 0, 0);
	public static final Color blue = new Color(Display.getDefault(), 0, 0, 255);
	public static final Color cyan = new Color(Display.getDefault(), 0, 255, 255);
	public static final Color green = new Color(Display.getDefault(), 0, 255, 0);
	public static final Color grey = new Color(Display.getDefault(), 128, 128, 128);
	public static final Color orange = new Color(Display.getDefault(), 255, 128, 0);
	public static final Color pink = new Color(Display.getDefault(), 255, 0, 255);
	public static final Color purple = new Color(Display.getDefault(), 128, 0, 255);
	public static final Color red = new Color(Display.getDefault(), 255, 0, 0);
	public static final Color yellow = new Color(Display.getDefault(), 255, 255, 0);
	public static final Color white = new Color(Display.getDefault(), 255, 255, 255);
	
	private Chart chart;
	private ArrayList<ILineSeries> series;
	
	private void setTheme(Chart c) {		
		c.getTitle().setForeground(yellow);
		
		c.setBackground(black);
		c.setBackgroundInPlotArea(black);
		
		IAxisSet axisset = chart.getAxisSet();
		
		axisset.getXAxis(0).getGrid().setForeground(black);
		axisset.getYAxis(0).getGrid().setForeground(black);
		
		axisset.getXAxis(0).getTick().setForeground(yellow);
		axisset.getYAxis(0).getTick().setForeground(yellow);
		axisset.getXAxis(0).getTitle().setForeground(yellow);
		axisset.getYAxis(0).getTitle().setForeground(yellow);
		
		chart.getLegend().setVisible(false);
	}
	
	public ChartBuilder(Shell shell) {
		chart = new Chart(shell, SWT.NONE);
		series = new ArrayList<>();
	}
	
	public ChartBuilder(Composite parent) {
		chart = new Chart(parent, SWT.NONE);
		series = new ArrayList<>();
	}
	
	public ChartBuilder setTitle(String title) {
		chart.getTitle().setText(title);
		return this;
	}
	
	public ChartBuilder setXAxisLabel(String label) {
		chart.getAxisSet().getXAxis(0).getTitle().setText(label);
		return this;
	}
	
	public ChartBuilder setYAxisLabel(String label) {
		chart.getAxisSet().getYAxis(0).getTitle().setText(label);
		return this;
	}
	
	public ChartBuilder addSeries(LineStyle style, String name, Color color, double[] XSeries, double[] YSeries) {
		ILineSeries ser = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, name);
		ser.setXSeries(XSeries);
		ser.setYSeries(YSeries);
		
		if (style == LineStyle.NONE) {
			ser.setLineStyle(LineStyle.NONE);
			ser.setSymbolType(PlotSymbolType.CIRCLE);
			ser.setSymbolColor(color);
			ser.setSymbolSize(3);
		} else {
			ser.setLineStyle(style);
			ser.setSymbolType(PlotSymbolType.NONE);
			ser.setLineColor(color);
		}
			
		if (series.size() > 0) {
			int YaxisId = chart.getAxisSet().createYAxis();
			IAxis yAxis = chart.getAxisSet().getYAxis(YaxisId);
			
			yAxis.getTick().setVisible(false);
	        yAxis.getTitle().setVisible(false);
	        yAxis.getGrid().setForeground(black);
	        
	        ser.setYAxisId(YaxisId);
	        
	        int XaxisId = chart.getAxisSet().createXAxis();
			IAxis xAxis = chart.getAxisSet().getXAxis(XaxisId);
			
			xAxis.getTick().setVisible(false);
	        xAxis.getTitle().setVisible(false);
	        xAxis.getGrid().setForeground(black);
	        
	        ser.setXAxisId(XaxisId);
		}
		
		series.add(ser);
		
		return this;
	}
	
	public ChartBuilder adjustRange() {
		adjustRange(chart);
		return this;
	}
	
	public ChartBuilder adjustRange(int index) { // where index means position in which it was added
		int XAxisId = series.get(index).getXAxisId();
		int YAxisId = series.get(index).getXAxisId();
		adjustRange(chart, XAxisId, YAxisId);
		return this;
	}
	
	public Chart build() {
		setTheme(chart);
		return chart;
	}
	
	
	public static void adjustRange(Chart c) {
		adjustRange(c, 0, 0);
	}
	
	public static void adjustRange(Chart c, int XAxisId, int YAxisId) {
		c.getAxisSet().adjustRange();
		if (c.getAxisSet().getAxes().length > 2) {
			Range XRange = c.getAxisSet().getXAxis(XAxisId).getRange();
			Range YRange = c.getAxisSet().getYAxis(YAxisId).getRange();
			for (IAxis i : c.getAxisSet().getXAxes()) {
				if (i.getId() != XAxisId) {
					i.setRange(XRange);
				}
			}
			for (IAxis i : c.getAxisSet().getYAxes()) {
				if (i.getId() != YAxisId) {
					i.setRange(YRange);
				}
			}
		}
	}
	
}
