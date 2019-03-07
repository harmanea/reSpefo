package cz.cuni.mff.respefo.clearCosmics;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.Listeners.FileExportItemListener;
import cz.cuni.mff.respefo.Listeners.MouseDragListener;
import cz.cuni.mff.respefo.Listeners.MouseWheelZoomListener;
import cz.cuni.mff.respefo.util.ChartBuilder;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Util;

public class ClearCosmicsItemListener implements SelectionListener {
	private static ClearCosmicsItemListener instance;
	
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	private Spectrum spectrum;
	
	private int activeIndex;
	private TreeSet<Integer> deletedIndexes;
	
	private double[] xSeries;
	private double[] ySeries;
	
	private boolean summary;
	
	private ClearCosmicsItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new ClearCosmicsItemListener");
	}
	
	public static ClearCosmicsItemListener getInstance() {
		if (instance == null) {
			instance = new ClearCosmicsItemListener();
		}
		
		return instance;
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "Clear Cosmics widget default selected");

		handle(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "Clear Cosmics widget selected");
		
		handle(event);
	}
	
	private void handle(SelectionEvent event) {
		ReSpefo.reset();
		
		String fileName = Util.openFileDialog(Util.SPECTRUM_LOAD);
		if (fileName == null) {
			LOGGER.log(Level.FINER, "File dialog returned null");
			return;
		}
		
		try {
			spectrum = Spectrum.createFromFile(fileName);
		} catch (SpefoException exception) {
			LOGGER.log(Level.WARNING, "Couldn't import file.", exception);
			MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			messageBox.setMessage("Couldn't import file.\n\nDebug message:\n" + exception.getMessage());
			messageBox.open();
			return;
		}
		
		xSeries = spectrum.getXSeries();
		ySeries = spectrum.getYSeries();

		activeIndex = 0;
		deletedIndexes = new TreeSet<>();
		
		createChart();
		
		ReSpefo.getScene().addSavedKeyListener(new ClearCosmicsKeyListener());
		
		ReSpefo.getScene().addSavedMouseWheelListener(new MouseWheelZoomListener());
	}
	
	private void createChart() {
		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		chart = ChartBuilder.from(ReSpefo.getScene()).setTitle(spectrum.getName()).setXAxisLabel("wavelength (Å)")
				.setYAxisLabel("relative flux I(λ)")
				.addScatterSeries(PlotSymbolType.CIRCLE, 2, "points", ChartBuilder.GREEN, xSeries, ySeries)
				.addScatterSeries(PlotSymbolType.CIRCLE, 2, "deleted", ChartBuilder.PINK, new double[] {}, new double[] {})
				.addScatterSeries(PlotSymbolType.CIRCLE, 3, "selected", deletedIndexes.contains(activeIndex) ? ChartBuilder.ORANGE : ChartBuilder.RED,
						new double[] { xSeries[activeIndex] }, new double[] { ySeries[activeIndex] })
				.adjustRange().build();
		
		ReSpefo.setChart(chart);
		
		MouseDragListener dragListener = new MouseDragListener(true);
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
		
		ClearCosmicsMouseListener mouseListener = new ClearCosmicsMouseListener();
		chart.getPlotArea().addMouseMoveListener(mouseListener);
		
		summary = false;
	}
	
	private void createSummaryChart() {
		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		chart = ChartBuilder.from(ReSpefo.getScene()).setTitle("press ESC to edit, press ENTER to save")
				.setXAxisLabel("wavelength (Å)").setYAxisLabel("relative flux I(λ)")
				.addScatterSeries(PlotSymbolType.CIRCLE, 1, "series", ChartBuilder.GREEN, xSeries, ySeries)
				.adjustRange().build();

		ReSpefo.setChart(chart);
		
		// add listeners		
		MouseDragListener dragListener = new MouseDragListener(true);
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
		summary = true;
	}
	
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
	private void updateYSeries(String name, double[] ySeries) {
		ILineSeries series = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries(name);
		
		series.setYSeries(ySeries);
	}
	
	private void updateSelectedSeries() {
		ILineSeries series = (ILineSeries) ReSpefo.getChart().getSeriesSet().getSeries("selected");
		series.setSymbolColor(deletedIndexes.contains(activeIndex) ? ChartBuilder.ORANGE : ChartBuilder.RED);
		series.setXSeries(new double[] {xSeries[activeIndex]});
		series.setYSeries(new double[] {ySeries[activeIndex]});
		
		ReSpefo.getChart().redraw();
	}
	
	public void selectPoint(int index) {
		if (index != activeIndex) {
			activeIndex = index;
			
			updateSelectedSeries();
		}
	}
	
	public void nextPoint() {
		if (activeIndex < xSeries.length - 1) {
			
			selectPoint(activeIndex + 1);
		}
	}
	
	public void previousPoint() {
		if (activeIndex > 0) {
			
			selectPoint(activeIndex - 1);
		}
	}
	
	public void movePoint(boolean up) {
		if (!deletedIndexes.contains(activeIndex)) {
			Chart chart = ReSpefo.getChart();
			ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries("points");
			IAxis yAxis = chart.getAxisSet().getYAxis(series.getYAxisId());
		
			double step = (yAxis.getRange().upper - yAxis.getRange().lower) / 400;
			
			spectrum.setY(activeIndex, spectrum.getY(activeIndex) + (up ? step : -step));
			
			updateAllSeries(false);
		}
	}
	
	public void delete() {
		changePoint(false);
	}
	
	public void insert() {
		changePoint(true);
	}
	
	// true - insert, false - delete
	private void changePoint(boolean insert) {
		if (insert) {
			if (deletedIndexes.contains(activeIndex)) {	
				deletedIndexes.remove(activeIndex);
			} else {
				return;
			}
		} else {
			if (!deletedIndexes.contains(activeIndex)) {
				deletedIndexes.add(activeIndex);
			} else {
				return;
			}
		}
		
		updateAllSeries(true);
	}
	
	private void updateAllSeries(boolean hopToNext) {
		List<Double> deletedXSeries = new ArrayList<>();
		List<Double> deletedYSeries = new ArrayList<>();
		
		for (Integer i : deletedIndexes) {
			deletedXSeries.add(spectrum.getX(i));
			deletedYSeries.add(spectrum.getY(i));
		}
		
		
		double[] remainingXSeries = IntStream.range(0, xSeries.length).filter(index -> !deletedIndexes.contains(index))
				.mapToDouble(index -> spectrum.getX(index)).toArray();
		
		double[] remainingYSeries = IntStream.range(0, ySeries.length).filter(index -> !deletedIndexes.contains(index))
				.mapToDouble(index -> spectrum.getY(index)).toArray();
	
		ySeries = MathUtils.intep(remainingXSeries, remainingYSeries, xSeries);
		
		updateYSeries("points", ySeries);
		updateLineSeries("deleted", deletedXSeries.stream().mapToDouble(Double::doubleValue).toArray(),
				deletedYSeries.stream().mapToDouble(Double::doubleValue).toArray());
		
		if (hopToNext && activeIndex < xSeries.length - 1) {
			activeIndex++;
		}
		updateSelectedSeries();
		
		ReSpefo.getChart().redraw();
	}
	
	public void enter() {
		if (!summary) {
			createSummaryChart();
		} else {

			double[] oldYSeries = spectrum.getYSeries();
			spectrum.setYSeries(ySeries);
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
}
