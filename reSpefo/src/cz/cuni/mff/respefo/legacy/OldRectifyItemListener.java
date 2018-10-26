package cz.cuni.mff.respefo.legacy;

import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Util;
import cz.cuni.mff.respefo.Listeners.DefaultMovementListener;

@Deprecated
public class OldRectifyItemListener implements SelectionListener {	
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
	
	private TreeSet<Point> cont = new TreeSet<>();
	private int index = 0;
	
	private boolean drag = false;
	private long startMillis;
	private int startX, startY;
	private int prevX, prevY;
	
	private Point getAt(int index) {
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
	
	private void adjustView() {
		Point p = getAt(index);
		Range Xrange = ReSpefo.getChart().getAxisSet().getXAxis(0).getRange();
		Range Yrange = ReSpefo.getChart().getAxisSet().getYAxis(0).getRange();
		
		
		Range xr = new Range(p.x - (Xrange.upper - Xrange.lower) / 2, p.x + (Xrange.upper - Xrange.lower) / 2);
		Range yr = new Range(p.y - (Yrange.upper - Yrange.lower) / 2, p.y + (Yrange.upper - Yrange.lower) / 2);
			
		for (IAxis a : ReSpefo.getChart().getAxisSet().getXAxes()) {
			a.setRange(xr);
		}
		for (IAxis a : ReSpefo.getChart().getAxisSet().getYAxes()) {
			a.setRange(yr);
		}
	}
	
	
	@Override
	public void widgetSelected(SelectionEvent event) {
		OldSpectrum spectrum;
		
		if (event != null) {
			spectrum = Util.importSpectrum();
			
			if (spectrum == null) {
				return;
			}
			
			ReSpefo.setOldSpectrum(spectrum);
		} else {
			spectrum = ReSpefo.getOldSpectrum();
		}
		
		if (cont.size() == 0) {
			cont.add(new Point(spectrum.getX(0), spectrum.getY(0)));
			cont.add(new Point(spectrum.getX(spectrum.size() - 1), spectrum.getY(spectrum.size() - 1)));
		}
		
		index = 0;
		
		Util.clearShellListeners();
		
		Chart chart = ReSpefo.getChart();
		
		if (chart != null) {
			chart.dispose();
		}
		chart = new ChartBuilder(ReSpefo.getScene()).setTitle(spectrum.name()).setXAxisLabel("wavelength (Å)").setYAxisLabel("flux")
				.addSeries(LineStyle.SOLID, "original", ChartBuilder.GREEN, spectrum.getXSeries(), spectrum.getYSeries())
				.addSeries(LineStyle.SOLID, "continuum", ChartBuilder.YELLOW, spectrum.getXSeries(), this.getIntepData(spectrum.getXSeries()))
				.addSeries(LineStyle.NONE, "points", ChartBuilder.WHITE, this.getXData(), this.getYData())
				.addSeries(LineStyle.NONE, "selected", ChartBuilder.RED, new double[] { getAt(index).x }, new double[] { getAt(index).y })
				.adjustRange().build();
        
        chart.getPlotArea().addMouseListener(new MouseListener() {

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
				drag = false;
				if ((e.stateMask & SWT.CTRL) != SWT.CTRL) { // single click
					if (System.currentTimeMillis() - startMillis < 50) {
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
				} else if (System.currentTimeMillis() - startMillis > 50) { // drag release
					Chart chart = ReSpefo.getChart();
					Range ChartXRange = chart.getAxisSet().getXAxis(0).getRange();
					Range ChartYRange = chart.getAxisSet().getYAxis(0).getRange();
					
					Rectangle bounds = chart.getPlotArea().getBounds();
					
					Range XRange = new Range(ChartXRange.lower + (ChartXRange.upper - ChartXRange.lower) * startX / bounds.width,
							ChartXRange.lower + (ChartXRange.upper - ChartXRange.lower) * e.x / bounds.width);
					Range YRange = new Range(ChartYRange.lower + (ChartYRange.upper - ChartYRange.lower) * (bounds.height - startY) / bounds.height,
							ChartYRange.lower + (ChartYRange.upper - ChartYRange.lower) * (bounds.height - e.y) / bounds.height);
					
					Point selected = null;
					int i = 0;
					for (Point p : cont) {
						if (XRange.lower < p.x && XRange.upper > p.x && YRange.lower < p.y && YRange.upper > p.y) {
							selected = p;
							index = i;
							break;
						}
						i++;
					}
					
					if (selected != null) {
						ILineSeries ser = (ILineSeries) chart.getSeriesSet().getSeries("selected");
						ser.setXSeries(new double[] {selected.x} );
						ser.setYSeries(new double[] {selected.y} );
					}
					
					chart.redraw();
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
        
        chart.getPlotArea().addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent arg0) {
				if (drag) {
					prevX = arg0.x;
					prevY = arg0.y;
					ReSpefo.getChart().redraw();
				}
			}
		});
        
        chart.getPlotArea().addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent arg0) {
				if (drag) {
					arg0.gc.drawRectangle(startX, startY, prevX - startX, prevY - startY);
				}
			}
		});
        
        ReSpefo.getShell().addKeyListener(new DefaultMovementListener());
        
        ReSpefo.getShell().addKeyListener(new RectifyKeyAdapter());
        
        ReSpefo.setChart(chart);
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}

	private class RectifyKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			Chart chart = ReSpefo.getChart();
			OldSpectrum spectrum = ReSpefo.getOldSpectrum();
			
			ILineSeries points = (ILineSeries) chart.getSeriesSet().getSeries("points");
			ILineSeries continuum = (ILineSeries) chart.getSeriesSet().getSeries("continuum");
			ILineSeries selected = (ILineSeries) chart.getSeriesSet().getSeries("selected");
			
			Range Yrange = chart.getAxisSet().getYAxis(0).getRange();
			Range Xrange = chart.getAxisSet().getXAxis(0).getRange();

			switch (e.keyCode) {
			case 'i':
				getAt(index).y += (Yrange.upper - Yrange.lower) / 400;
				points.setYSeries(getYData());
				continuum.setYSeries(getIntepData(spectrum.getXSeries()));
				selected.setYSeries(new double[] { getAt(index).y });
				break;
			case 'k':
				getAt(index).y -= (Yrange.upper - Yrange.lower) / 400;
				points.setYSeries(getYData());
				continuum.setYSeries(getIntepData(spectrum.getXSeries()));
				selected.setYSeries(new double[] { getAt(index).y });
				break;
			case 'j':
				getAt(index).x -= (Xrange.upper - Xrange.lower) / 400;
				points.setXSeries(getXData());
				continuum.setYSeries(getIntepData(spectrum.getXSeries()));
				selected.setXSeries(new double[] { getAt(index).x });
				break;
			case 'l':
				getAt(index).x += (Xrange.upper - Xrange.lower) / 400;
				points.setXSeries(getXData());
				continuum.setYSeries(getIntepData(spectrum.getXSeries()));
				selected.setXSeries(new double[] { getAt(index).x });
				break;
			case 'n':
				if (index > 0) {
					index--;
					selected.setXSeries(new double[] { getAt(index).x });
					selected.setYSeries(new double[] { getAt(index).y });
					Point p = getAt(index);
					if (Xrange.lower > p.x || Xrange.upper < p.x || Yrange.lower > p.y || Yrange.upper < p.y) {
						adjustView();
					}
				}
				break;
			case 'm':
				if (index + 1 < cont.size()) {
					index++;
					selected.setXSeries(new double[] { getAt(index).x });
					selected.setYSeries(new double[] { getAt(index).y });
					Point q = getAt(index);
					if (Xrange.lower > q.x || Xrange.upper < q.x || Yrange.lower > q.y || Yrange.upper < q.y) {
						adjustView();
					}
				}
				break;
			case 'p':
				adjustView();
				break;
			case 'x':
				if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
					MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.YES | SWT.NO);
					mb.setMessage("Are you sure?");
					if (mb.open() == SWT.YES) {
						cont.clear();
						cont.add(new Point(spectrum.getX(0), spectrum.getY(0)));
						cont.add(new Point(spectrum.getX(spectrum.size() - 1), spectrum.getY(spectrum.size() - 1)));
						
						index = 0;
						
						points.setXSeries(getXData());
						points.setYSeries(getYData());
						
						continuum.setYSeries(getIntepData(spectrum.getXSeries()));
						
						selected.setXSeries(new double[] { getAt(index).x });
						selected.setYSeries(new double[] { getAt(index).y });
					}	
				}
				break;
			case SWT.INSERT:
				Point p = new Point((Xrange.upper + Xrange.lower)/2, (Yrange.upper + Yrange.lower)/2);
				cont.add(p);
				
				points.setXSeries(getXData());
				points.setYSeries(getYData());
				
				continuum.setYSeries(getIntepData(spectrum.getXSeries()));
				
				index = cont.headSet(p).size();
				
				selected.setXSeries(new double[] { p.x });
				selected.setYSeries(new double[] { p.y });
				break;
			case SWT.DEL:
				if (cont.size() > 1) {
					cont.remove(getAt(index));
					
					if (index > 0) {
						index--;
					}
					
					points.setXSeries(getXData());
					points.setYSeries(getYData());
					
					continuum.setYSeries(getIntepData(spectrum.getXSeries()));
					
					selected.setXSeries(new double[] { getAt(index).x });
					selected.setYSeries(new double[] { getAt(index).y });
				}
				break;
			case SWT.CR:
				ReSpefo.getShell().addKeyListener(new KeyAdapter() {
					OldSpectrum old = spectrum;
					
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.BS) {
							ReSpefo.setOldSpectrum(old);
							ReSpefo.getShell().removeKeyListener(this);
							widgetSelected(null);
						}
					}
				});
					
				double[] YSeries = Util.divideArrayValues(spectrum.getYSeries(), continuum.getYSeries());
				OldSpectrum s = new OldSpectrum(spectrum.getXSeries(), YSeries, spectrum.name());
					
				ReSpefo.setOldSpectrum(s);
				if (chart != null) {
					chart.dispose();
				}
				chart = new ChartBuilder(ReSpefo.getShell()).setTitle(s.name()).setXAxisLabel("wavelength (Å)").setYAxisLabel("relative flux I(λ)")
						.addSeries(LineStyle.SOLID, "series", ChartBuilder.GREEN, s.getXSeries(), s.getYSeries())
						.adjustRange().build();
					
				ReSpefo.setChart(chart);
				
				ReSpefo.getShell().removeKeyListener(this);				
				break;
			}
			chart.redraw();
		}
	}
}
