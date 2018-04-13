package cz.cuni.mff.respefo.Listeners;

import java.util.Iterator;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.Range;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

import cz.cuni.mff.respefo.ChartBuilder;
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
	
	private TreeSet<Point> cont;
	private int index = 0;
	
	public Point getAt(int index) {
		if (index < 0 || index >= cont.size()) {
			return null;
		}
		
		Iterator<Point> it = cont.iterator();
		int i = 0;
		Point ret = null;
		while (it.hasNext() && i <= index) {
			ret = it.next();
			i++;
		}
		return ret;
		
	}
	
	private double[] getIntepData(double[] xinter) {
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
	
	private double[] getXData() {
		double[] X = new double[cont.size()];
		
		int i = 0;
		for (Point p : cont) {
			X[i] = p.x;
			i++;
		}
		
		return X;
	}
	
	private double[] getYData() {
		double[] Y = new double[cont.size()];
		
		int i = 0;
		for (Point p : cont) {
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
		
		ReSpefo.setSpectrum(spectrum);
		
		cont = new TreeSet<>();
		cont.add(new Point(spectrum.getX(0), spectrum.getY(0)));
		cont.add(new Point(spectrum.getX(spectrum.size() - 1), spectrum.getY(spectrum.size() - 1)));
		
		for (Listener l : ReSpefo.getShell().getListeners(SWT.KeyDown)) {
			ReSpefo.getShell().removeListener(SWT.KeyDown, l);
		}
		
		for (Listener l : ReSpefo.getShell().getListeners(SWT.MouseDown)) {
			ReSpefo.getShell().removeListener(SWT.MouseDown, l);
		}
		
		Chart chart = ReSpefo.getChart();
		
		if (chart != null) {
			chart.dispose();
		}
		chart = new ChartBuilder(ReSpefo.getShell()).setTitle(spectrum.name()).setXAxisLabel("wavelength (Å)").setYAxisLabel("flux")
				.addSeries(LineStyle.SOLID, "original", ChartBuilder.green, spectrum.getXSeries(), spectrum.getYSeries())
				.addSeries(LineStyle.SOLID, "continuum", ChartBuilder.yellow, spectrum.getXSeries(), this.getIntepData(spectrum.getXSeries()))
				.addSeries(LineStyle.NONE, "points", ChartBuilder.white, this.getXData(), this.getYData())
				.addSeries(LineStyle.NONE, "selected", ChartBuilder.red, new double[] { cont.first().x }, new double[] { cont.first().y })
				.adjustRange().pack();
        
        chart.getPlotArea().addMouseListener(new MouseListener() {

			@Override
			public void mouseDown(MouseEvent e) {
				Chart chart = ReSpefo.getChart();
				Rectangle bounds = chart.getPlotArea().getBounds();
				Range XRange = chart.getAxisSet().getXAxis(0).getRange();
				Range YRange = chart.getAxisSet().getYAxis(0).getRange();
				double x = XRange.lower + ((XRange.upper - XRange.lower) * ((double) e.x / bounds.width));
				double y = YRange.lower + ((YRange.upper - YRange.lower) * ((double) (bounds.height - e.y) / bounds.height));
				Point p = new Point(x, y);
				cont.add(p);
				
				ILineSeries continuum = (ILineSeries) chart.getSeriesSet().getSeries("continuum");
				ILineSeries original = (ILineSeries) chart.getSeriesSet().getSeries("original");
				continuum.setYSeries(getIntepData(original.getXSeries()));
				
				ILineSeries points = (ILineSeries) chart.getSeriesSet().getSeries("points");
				points.setXSeries(getXData());
				points.setYSeries(getYData());
				
				index = cont.headSet(p).size();
				ILineSeries selected = (ILineSeries) chart.getSeriesSet().getSeries("selected");
				selected.setXSeries(new double[] {x} );
				selected.setYSeries(new double[] {y} );
				
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
				
				ILineSeries points = (ILineSeries) chart.getSeriesSet().getSeries("points");
				ILineSeries continuum = (ILineSeries) chart.getSeriesSet().getSeries("continuum");
				ILineSeries selected = (ILineSeries) chart.getSeriesSet().getSeries("selected");
				
				Range Yrange = chart.getAxisSet().getYAxis(0).getRange();
				Range Xrange = chart.getAxisSet().getXAxis(0).getRange();

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
					ChartBuilder.adjustRange(chart);
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
				case 'i':
					getAt(index).y += (Yrange.upper - Yrange.lower) / 400;
					points.setYSeries(getYData());
					continuum.setYSeries(getIntepData(ReSpefo.getSpectrum().getXSeries()));
					selected.setYSeries(new double[] { getAt(index).y });
					break;
				case 'k':
					getAt(index).y -= (Yrange.upper - Yrange.lower) / 400;
					points.setYSeries(getYData());
					continuum.setYSeries(getIntepData(ReSpefo.getSpectrum().getXSeries()));
					selected.setYSeries(new double[] { getAt(index).y });
					break;
				case 'j':
					getAt(index).x -= (Xrange.upper - Xrange.lower) / 400;
					points.setXSeries(getXData());
					continuum.setYSeries(getIntepData(ReSpefo.getSpectrum().getXSeries()));
					selected.setXSeries(new double[] { getAt(index).x });
					break;
				case 'l':
					getAt(index).x += (Xrange.upper - Xrange.lower) / 400;
					points.setXSeries(getXData());
					continuum.setYSeries(getIntepData(ReSpefo.getSpectrum().getXSeries()));
					selected.setXSeries(new double[] { getAt(index).x });
					break;
				case 'n':
					if (index > 0) {
						index--;
						selected.setXSeries(new double[] { getAt(index).x });
						selected.setYSeries(new double[] { getAt(index).y });
					}
					break;
				case 'm':
					if (index + 1 < cont.size()) {
						index++;
						selected.setXSeries(new double[] { getAt(index).x });
						selected.setYSeries(new double[] { getAt(index).y });
					}
					break;
				case SWT.DEL:
					if (cont.size() > 1) {
						cont.remove(getAt(index));
						index--;
						
						points.setXSeries(getXData());
						points.setYSeries(getYData());
						
						continuum.setYSeries(getIntepData(ReSpefo.getSpectrum().getXSeries()));
						
						selected.setXSeries(new double[] { getAt(index).x });
						selected.setYSeries(new double[] { getAt(index).y });
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
