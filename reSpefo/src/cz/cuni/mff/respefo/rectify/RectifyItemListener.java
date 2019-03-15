package cz.cuni.mff.respefo.rectify;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.Listeners.AbstractSelectionListener;
import cz.cuni.mff.respefo.Listeners.FileExportItemListener;
import cz.cuni.mff.respefo.Listeners.MouseDragListener;
import cz.cuni.mff.respefo.Listeners.MouseWheelZoomListener;
import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.ChartBuilder;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;

public class RectifyItemListener extends AbstractSelectionListener {
	private static RectifyItemListener instance;
	
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	private Spectrum spectrum;
	private RectifyPoints points;
	
	private boolean summary;
	
	private RectifyItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new RectifyItemListener");
		
		points = new RectifyPoints();
	}
	
	public static RectifyItemListener getInstance() {
		if (instance == null) {
			instance = new RectifyItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		ReSpefo.reset();
		
		String fileName = FileUtils.fileOpenDialog(FileType.SPECTRUM);
		
		if (fileName == null) {
			LOGGER.log(Level.FINER, "File dialog returned null");
			return;
		}
		
		try {
			spectrum = Spectrum.createFromFile(fileName);
		} catch (SpefoException exception) {
			Message.error("Couldn't import file.", exception);
			return;
		}
		
		if (points.getCount() == 0) {
			points.reset(spectrum.getX(0), spectrum.getY(0), spectrum.getX(spectrum.getSize() - 1), spectrum.getY(spectrum.getSize() - 1));
		}
		
		createChart();

		ReSpefo.getScene().addSavedKeyListener(new RectifyKeyListener());
		
		ReSpefo.getScene().addSavedMouseWheelListener(new MouseWheelZoomListener());
	}
	
	private void createChart() {
		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		chart = ChartBuilder.in(ReSpefo.getScene()).setTitle(spectrum.getName()).setXAxisLabel("wavelength (Å)")
				.setYAxisLabel("relative flux")
				.addLineSeries(LineStyle.SOLID, "original", ChartBuilder.GREEN,
						spectrum.getXSeries(), spectrum.getYSeries())
				.addLineSeries(LineStyle.SOLID, "continuum", ChartBuilder.YELLOW,
						spectrum.getXSeries(), points.getIntepData(spectrum.getXSeries()))
				.addScatterSeries(PlotSymbolType.CIRCLE, 3, "points", ChartBuilder.WHITE,
						points.getXCoordinates(), points.getYCoordinates())
				.addScatterSeries(PlotSymbolType.CIRCLE, 3, "selected", ChartBuilder.RED,
						new double[] { points.getActiveX() }, new double[] { points.getActiveY() })
				.adjustRange().build();
		
		ReSpefo.setChart(chart);
			
		MouseDragListener dragListener = new MouseDragListener(true);
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
		
		RectifyMouseListener mouseListener = new RectifyMouseListener();
		chart.getPlotArea().addMouseListener(mouseListener);
		chart.getPlotArea().addMouseMoveListener(mouseListener);
		
		summary = false;
	}
	
	private void createSummaryChart() {
		ILineSeries continuum = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries("continuum");

		double[] ySeries = ArrayUtils.divideArrayValues(spectrum.getYSeries(), continuum.getYSeries());

		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		chart = ChartBuilder.in(ReSpefo.getScene()).setTitle("press ESC to edit, press ENTER to save")
				.setXAxisLabel("wavelength (Å)").setYAxisLabel("relative flux I(λ)")
				.addLineSeries(LineStyle.SOLID, "series", ChartBuilder.GREEN, spectrum.getXSeries(), ySeries)
				.adjustRange().build();

		ReSpefo.setChart(chart);
		
		// add listeners		
		MouseDragListener dragListener = new MouseDragListener(true);
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
		
		summary = true;
	}
	
	private void updateLineSeries(String name, double[] xSeries, double[] ySeries) {
		ILineSeries series = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries(name);
		
		series.setXSeries(xSeries);
		series.setYSeries(ySeries);
	}
	
	@SuppressWarnings("unused")
	private void updateXSeries(String name, double[] xSeries) {
		ILineSeries series = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries(name);
		
		series.setXSeries(xSeries);
	}
	
	private void updateYSeries(String name, double[] ySeries) {
		ILineSeries series = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries(name);
		
		series.setYSeries(ySeries);
	}
	
	public void previousPoint() {
		selectPoint(points.getActiveIndex() - 1);
	}
	
	public void nextPoint() {
		selectPoint(points.getActiveIndex() + 1);
	}
	
	public void selectPoint(int index) {
		if (!summary && index != points.getActiveIndex()) {
			points.setActivePoint(index);
			
			updateLineSeries("selected", new double[] { points.getActiveX() }, new double[] { points.getActiveY() });
			
			ReSpefo.getChart().redraw();
		}
	}
	
	public void addPoint(double x, double y) {
		if (!summary) {
			points.addPoint(x, y);
			
			updateYSeries("continuum", points.getIntepData(spectrum.getXSeries()));
			updateLineSeries("points", points.getXCoordinates(), points.getYCoordinates());
			updateLineSeries("selected", new double[] { points.getActiveX() }, new double[] { points.getActiveY() });
			
			ReSpefo.getChart().redraw();
		}
	}
	
	public void moveUp() {
		if (!summary) {
			Range yRange = ReSpefo.getChart().getAxisSet().getYAxis(0).getRange();

			points.movePoint(0, (yRange.upper - yRange.lower) / 400, points.getActiveIndex());

			updateYSeries("continuum", points.getIntepData(spectrum.getXSeries()));
			updateLineSeries("points", points.getXCoordinates(), points.getYCoordinates());
			updateLineSeries("selected", new double[] { points.getActiveX() }, new double[] { points.getActiveY() });
		}
	}

	public void moveDown() {
		if (!summary) {
			Range yRange = ReSpefo.getChart().getAxisSet().getYAxis(0).getRange();

			points.movePoint(0, -(yRange.upper - yRange.lower) / 400, points.getActiveIndex());

			updateYSeries("continuum", points.getIntepData(spectrum.getXSeries()));
			updateLineSeries("points", points.getXCoordinates(), points.getYCoordinates());
			updateLineSeries("selected", new double[] { points.getActiveX() }, new double[] { points.getActiveY() });
		}
	}

	public void moveLeft() {
		if (!summary) {
			Range xRange = ReSpefo.getChart().getAxisSet().getXAxis(0).getRange();

			points.movePoint(-(xRange.upper - xRange.lower) / 400, 0, points.getActiveIndex());

			updateYSeries("continuum", points.getIntepData(spectrum.getXSeries()));
			updateLineSeries("points", points.getXCoordinates(), points.getYCoordinates());
			updateLineSeries("selected", new double[] { points.getActiveX() }, new double[] { points.getActiveY() });
		}
	}

	public void moveRight() {
		if (!summary) {
			Range xRange = ReSpefo.getChart().getAxisSet().getXAxis(0).getRange();

			points.movePoint((xRange.upper - xRange.lower) / 400, 0, points.getActiveIndex());

			updateYSeries("continuum", points.getIntepData(spectrum.getXSeries()));
			updateLineSeries("points", points.getXCoordinates(), points.getYCoordinates());
			updateLineSeries("selected", new double[] { points.getActiveX() }, new double[] { points.getActiveY() });
		}
	}

	public void center() {
		if (!summary) {
			double x = points.getActiveX();
			double y = points.getActiveY();

			Range xRange = ReSpefo.getChart().getAxisSet().getXAxis(0).getRange();
			Range yRange = ReSpefo.getChart().getAxisSet().getYAxis(0).getRange();

			Range xr = new Range(x - (xRange.upper - xRange.lower) / 2, x + (xRange.upper - xRange.lower) / 2);
			Range yr = new Range(y - (yRange.upper - yRange.lower) / 2, y + (yRange.upper - yRange.lower) / 2);

			for (IAxis a : ReSpefo.getChart().getAxisSet().getXAxes()) {
				a.setRange(xr);
			}
			for (IAxis a : ReSpefo.getChart().getAxisSet().getYAxes()) {
				a.setRange(yr);
			}
		}
	}

	public void enter() {
		if (!summary) {
			createSummaryChart();
		} else {
			ILineSeries series = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries("series");
			double[] oldYSeries = spectrum.getYSeries();
			spectrum.setYSeries(series.getYSeries());
			ReSpefo.setSpectrum(spectrum);
			if (FileExportItemListener.getInstance().export(spectrum)) {
				ReSpefo.reset();

			} else {
				spectrum.setYSeries(oldYSeries);
				ReSpefo.setSpectrum(null);
			}
		}
	}
	
	public void escape() {
		if (summary) {
			createChart();
		}
	}

	public void insert() {
		if (!summary) {
			Chart chart = ReSpefo.getChart();

			Range yRange = chart.getAxisSet().getYAxis(0).getRange();
			Range xRange = chart.getAxisSet().getXAxis(0).getRange();

			points.addPoint((xRange.upper + xRange.lower) / 2, (yRange.upper + yRange.lower) / 2);

			updateYSeries("continuum", points.getIntepData(spectrum.getXSeries()));
			updateLineSeries("points", points.getXCoordinates(), points.getYCoordinates());
			updateLineSeries("selected", new double[] { points.getActiveX() }, new double[] { points.getActiveY() });
		}
	}

	public void delete() {
		if (!summary) {
			points.removePoint(points.getActiveIndex());

			updateYSeries("continuum", points.getIntepData(spectrum.getXSeries()));
			updateLineSeries("points", points.getXCoordinates(), points.getYCoordinates());
			updateLineSeries("selected", new double[] { points.getActiveX() }, new double[] { points.getActiveY() });
		}
	}

	public void clearPoints() {
		if (!summary) {
			if (Message.question("Do you want to clear all points?") == SWT.YES) {
				points.reset(spectrum.getX(0), spectrum.getY(0), spectrum.getX(spectrum.getSize() - 1),
						spectrum.getY(spectrum.getSize() - 1));

				updateYSeries("continuum", points.getIntepData(spectrum.getXSeries()));
				updateLineSeries("points", points.getXCoordinates(), points.getYCoordinates());
				updateLineSeries("selected", new double[] { points.getActiveX() }, new double[] { points.getActiveY() });
			}
		}
	}
}
