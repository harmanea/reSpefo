package cz.cuni.mff.respefo.Listeners;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.Range;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.Util;

public class RectifyItemListener implements SelectionListener {
	private class Point implements Comparable<Point>{
		protected double x;
		protected double y;
		
		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Point o) {
			return this.x == o.x ? Double.compare(this.y, o.y) : Double.compare(this.x, o.x);
		}
	}
	
	private SortedSet<Point> cont;
	
	private double[] getData(double[] xinter) {
		double[] X = new double[cont.size()];
		double[] Y = new double[cont.size()];
		
		int i = 0;
		for (Point p : cont) {
			X[i] = p.x;
			Y[i] = p.y;
			i++;
		}
		
		return Util.intep(X, Y, xinter);
	}
	
	private double[] getXData(double[] xinter) {
		double[] X = new double[cont.size()];
		double[] Y = new double[cont.size()];
		
		int i = 0;
		for (Point p : cont) {
			X[i] = p.x;
			Y[i] = p.y;
			i++;
		}
		
		return X;
	}
	private double[] getYData(double[] xinter) {
		double[] X = new double[cont.size()];
		double[] Y = new double[cont.size()];
		
		int i = 0;
		for (Point p : cont) {
			X[i] = p.x;
			Y[i] = p.y;
			i++;
		}
		
		return Y;
	}
	
	
	@Override
	public void widgetSelected(SelectionEvent event) {
		Spectrum spectrum = Util.importSpectrum();
		
		if (spectrum == null) {
			return;
		}
		
		cont = new TreeSet<>();
		cont.add(new Point(spectrum.getX(0), spectrum.getY(0)));
		cont.add(new Point(spectrum.getX(spectrum.size() - 1), spectrum.getY(spectrum.size() - 1)));
		
		Chart chart = ReSpefo.getChart();
		
		if (chart != null) {
			chart.dispose();
		}
		chart = new Chart(ReSpefo.getShell(), SWT.NONE);
		
		chart.getTitle().setText(spectrum.name());
        chart.getAxisSet().getXAxis(0).getTitle().setText("wavelength (Å)");
        chart.getAxisSet().getYAxis(0).getTitle().setText("flux");
        
        Color black = new Color(Display.getDefault(), 0, 0, 0);
		Color green = new Color(Display.getDefault(), 0, 255, 0);
		Color blue = new Color(Display.getDefault(), 0, 0, 255);
		Color yellow = new Color(Display.getDefault(), 255, 255, 0);
		
		int axisId = chart.getAxisSet().createYAxis();
        IAxis yAxis2 = chart.getAxisSet().getYAxis(axisId);
        yAxis2.getTick().setVisible(false);
        yAxis2.getTitle().setVisible(false);
        yAxis2.getGrid().setForeground(black);
        int axisId2 = chart.getAxisSet().createXAxis();
        IAxis xAxis2 = chart.getAxisSet().getXAxis(axisId2);
        xAxis2.getTick().setVisible(false);
        xAxis2.getTitle().setVisible(false);
        xAxis2.getGrid().setForeground(black);
        
        int axisId3 = chart.getAxisSet().createYAxis();
        IAxis yAxis3 = chart.getAxisSet().getYAxis(axisId3);
        yAxis3.getTick().setVisible(false);
        yAxis3.getTitle().setVisible(false);
        yAxis3.getGrid().setForeground(black);
        int axisId4 = chart.getAxisSet().createXAxis();
        IAxis xAxis4 = chart.getAxisSet().getXAxis(axisId4);
        xAxis4.getTick().setVisible(false);
        xAxis4.getTitle().setVisible(false);
        xAxis4.getGrid().setForeground(black);
        
        // create line series
        ILineSeries original = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "original");
        original.setXSeries(spectrum.getXSeries());
        original.setYSeries(spectrum.getYSeries());
        original.setLineStyle(LineStyle.SOLID);
        
        ILineSeries continuum = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "continuum");
        continuum.setXSeries(spectrum.getXSeries());
        continuum.setYSeries(this.getData(spectrum.getXSeries()));
        continuum.setLineStyle(LineStyle.SOLID);     
        
        // assign series to second Y axis
        continuum.setYAxisId(axisId);
        continuum.setXAxisId(axisId2);
        
        ILineSeries points = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "points");
        points.setXSeries(this.getXData(null));
        points.setYSeries(this.getYData(null));
        points.setLineStyle(LineStyle.NONE);
        
        IAxisSet axisset = chart.getAxisSet();
        
        chart.setBackground(black);
        chart.setBackgroundInPlotArea(black);
        axisset.getXAxis(0).getGrid().setForeground(black);
        axisset.getYAxis(0).getGrid().setForeground(black);
        
        //original.setSymbolSize(1);
        original.setSymbolColor(green);
        original.setSymbolType(PlotSymbolType.NONE);
        original.setLineColor(green);
        
        
        //continuum.setSymbolSize(1);
        continuum.setSymbolColor(yellow);
        continuum.setSymbolType(PlotSymbolType.NONE);
        continuum.setLineColor(yellow);
        
        
        axisset.getXAxis(0).getTick().setForeground(yellow);
        axisset.getYAxis(0).getTick().setForeground(yellow);
        chart.getTitle().setForeground(yellow);
        axisset.getXAxis(0).getTitle().setForeground(yellow);
        axisset.getYAxis(0).getTitle().setForeground(yellow);
        
        chart.getLegend().setVisible(false);
        
        chart.getAxisSet().adjustRange();
        axisset.getXAxis(axisId2).setRange(axisset.getXAxis(0).getRange());
        axisset.getYAxis(axisId).setRange(axisset.getYAxis(0).getRange());
        axisset.getXAxis(axisId4).setRange(axisset.getXAxis(0).getRange());
        axisset.getYAxis(axisId3).setRange(axisset.getYAxis(0).getRange());
        
        chart.getPlotArea().addMouseListener(new MouseListener() {

			@Override
			public void mouseDown(MouseEvent e) {
				Chart chart = ReSpefo.getChart();
				Rectangle bounds = chart.getPlotArea().getBounds();
				Range XRange = chart.getAxisSet().getXAxis(0).getRange();
				Range YRange = chart.getAxisSet().getYAxis(0).getRange();
				double x = XRange.lower + ((XRange.upper - XRange.lower) * ((double) e.x / bounds.width));
				double y = YRange.lower
						+ ((YRange.upper - YRange.lower) * ((double) (bounds.height - e.y) / bounds.height));
				cont.add(new Point(x, y));
				
				ILineSeries continuum = (ILineSeries) chart.getSeriesSet().getSeries("continuum");
				ILineSeries original = (ILineSeries) chart.getSeriesSet().getSeries("original");
				//continuum.setXSeries(getXdata());
				continuum.setYSeries(getData(original.getXSeries()));
				
				ILineSeries points = (ILineSeries) chart.getSeriesSet().getSeries("points");
				points.setXSeries(getXData(null));
				points.setYSeries(getYData(null));
				
				chart.redraw();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

		});
        
        ReSpefo.getShell().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				Chart chart = ReSpefo.getChart();

				switch (e.keyCode) {
				case 'w':
				case SWT.ARROW_UP: // arrow up
					for (IAxis i : chart.getAxisSet().getYAxes()) {
						i.scrollUp();
					}
					break;
				case 'a':
				case SWT.ARROW_LEFT: // arrow left
					for (IAxis i : chart.getAxisSet().getXAxes()) {
						i.scrollDown();
					}
					break;
				case 's':
				case SWT.ARROW_DOWN: // arrow down
					for (IAxis i : chart.getAxisSet().getYAxes()) {
						i.scrollDown();
					}
					break;
				case 'd':
				case SWT.ARROW_RIGHT: // arrow right
					for (IAxis i : chart.getAxisSet().getXAxes()) {
						i.scrollUp();
					}
					break;
				case SWT.SPACE: // space
					ILineSeries original = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries("original");
					//chart.getAxisSet().adjustRange();
					getData(original.getXSeries());
					break;
				case '+': // +
				case 16777259:
				case 49:
					chart.getAxisSet().zoomIn();
					break;
				case '-': // -
				case 16777261:
				case 47:
					chart.getAxisSet().zoomOut();
					break;
				case SWT.KEYPAD_8: // NumPad up
					for (IAxis i : chart.getAxisSet().getYAxes()) {
						i.zoomIn();;
					}
					break;
				case SWT.KEYPAD_2: // NumPad down
					for (IAxis i : chart.getAxisSet().getYAxes()) {
						i.zoomOut();;
					}
					break;
				case SWT.KEYPAD_4: // NumPad left
					for (IAxis i : chart.getAxisSet().getXAxes()) {
						i.zoomOut();;
					}
					break;
				case SWT.KEYPAD_6: // NumPad right
					for (IAxis i : chart.getAxisSet().getXAxes()) {
						i.zoomIn();;
					}
					break;
				}
				chart.redraw();
			}
		});
        
        ReSpefo.setChart(chart);
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}

}
