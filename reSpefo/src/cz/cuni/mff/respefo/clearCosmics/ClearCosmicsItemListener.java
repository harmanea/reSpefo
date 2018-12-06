package cz.cuni.mff.respefo.clearCosmics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.Util;
import cz.cuni.mff.respefo.Listeners.MouseDragListener;
import cz.cuni.mff.respefo.Listeners.MouseWheelZoomListener;

public class ClearCosmicsItemListener implements SelectionListener {
	private static ClearCosmicsItemListener instance;
	
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	private Spectrum spectrum;
	
	private int activeIndex;
	private TreeSet<Integer> deletedIndexes;
	
	private double[] xSeries;
	private double[] ySeries;
	
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
	public void widgetDefaultSelected(SelectionEvent e) {
		LOGGER.log(Level.FINEST, "Clear Cosmics widget default selected");

		handle(e);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		LOGGER.log(Level.FINEST, "Clear Cosmics widget selected");
		
		handle(e);
	}
	
	private void handle(SelectionEvent e) {
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
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			mb.setMessage("Couldn't import file.\n\nDebug message:\n" + exception.getMessage());
			mb.open();
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
		chart = new ChartBuilder(ReSpefo.getScene()).setTitle(spectrum.getName()).setXAxisLabel("wavelength (Å)")
				.setYAxisLabel("relative flux I(λ)")
				.addScatterSeries(PlotSymbolType.CIRCLE, 1, "points", ChartBuilder.GREEN, xSeries, ySeries)
				.addScatterSeries(PlotSymbolType.CIRCLE, 1, "deleted", ChartBuilder.PINK, new double[] {}, new double[] {})
				.addScatterSeries(PlotSymbolType.CIRCLE, 2, "selected", ChartBuilder.RED,
						new double[] { xSeries[activeIndex] }, new double[] { ySeries[activeIndex] })
				.adjustRange().build();
		
		ReSpefo.setChart(chart);
		
		MouseDragListener dragListener = new MouseDragListener(true);
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
		
		ClearCosmicsMouseListener mouseListener = new ClearCosmicsMouseListener();
		chart.getPlotArea().addMouseMoveListener(mouseListener);
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
	
	public void selectPoint(int index) {
		if (index != activeIndex) {
			activeIndex = index;
			
			updateLineSeries("selected", new double[] {xSeries[activeIndex]},  new double[] {ySeries[activeIndex]});
			
			ReSpefo.getChart().redraw();
		}
	}
	
	public void delete() {
		deletedIndexes.add(activeIndex);
		
		List<Double> xList = Arrays.stream(xSeries).map(Double::valueOf).boxed().collect(Collectors.toList());
		xList.remove(activeIndex);
		double[] newXSeries = xList.stream().mapToDouble(Double::doubleValue).toArray();
		
		List<Double> yList = Arrays.stream(ySeries).map(Double::valueOf).boxed().collect(Collectors.toList());
		yList.remove(activeIndex);
		double[] newYSeries = yList.stream().mapToDouble(Double::doubleValue).toArray();
		
		ySeries[activeIndex] = Util.intep(newXSeries, newYSeries, new double[] { xSeries[activeIndex] })[0];
		
		updateLineSeries("points", xSeries, ySeries);
		updateYSeries("selected", new double[] { ySeries[activeIndex] });
		
		double[] deletedXSeries = new double[deletedIndexes.size()];
		double[] deletedYSeries = new double[deletedIndexes.size()];
		
		int i = 0;
		for (Iterator<Integer> it = deletedIndexes.iterator(); it.hasNext(); i++) {
			int next = it.next();
			deletedXSeries[i] = spectrum.getX(next);
			deletedYSeries[i] = spectrum.getY(next);
		}
		
		updateLineSeries("deleted", deletedXSeries, deletedYSeries);
		
		ReSpefo.getChart().redraw();
	}
	
	public void insert() {
		if (deletedIndexes.contains(activeIndex)) {	
			deletedIndexes.remove(activeIndex);
			
			double[] deletedXSeries = new double[deletedIndexes.size()];
			double[] deletedYSeries = new double[deletedIndexes.size()];
			
			int i = 0;
			for (Iterator<Integer> it = deletedIndexes.iterator(); it.hasNext(); i++) {
				int next = it.next();
				deletedXSeries[i] = spectrum.getX(next);
				deletedYSeries[i] = spectrum.getY(next);
			}
			
			updateLineSeries("deleted", deletedXSeries, deletedYSeries);
			
			ySeries[activeIndex] = spectrum.getY(activeIndex);
			updateYSeries("points", ySeries);
			updateYSeries("selected", new double[] { ySeries[activeIndex] });
		}
	}
}
