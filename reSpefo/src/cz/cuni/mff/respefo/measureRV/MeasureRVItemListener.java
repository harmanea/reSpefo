package cz.cuni.mff.respefo.measureRV;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.Util;
import cz.cuni.mff.respefo.Listeners.MouseWheelZoomListener;

public class MeasureRVItemListener implements SelectionListener {
	private static MeasureRVItemListener instance;
	
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	private static final double SPEED_OF_LIGHT = 299792.458;
	
	private RVMeasurements rvms;
	private RVResults results;
	private int index;
	private boolean summary;
	
	private Spectrum spectrum;
	private double[] xSeries;
	private double[] ySeries;
	private double deltaRV;
	
	private double shift;
	
	private Composite container; // holds current chart
	private List list;
	private List list2;

	private MeasureRVItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new MeasureRVItemListener");
	}
	
	public static MeasureRVItemListener getInstance() {
		if (instance == null) {
			instance = new MeasureRVItemListener();
		}
		
		return instance;
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		LOGGER.log(Level.FINEST, "Measure RV widget default selected");
		handle(e);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		LOGGER.log(Level.FINEST, "Measure RV widget selected");
		handle(e);
	}

	private void handle(SelectionEvent e) {
		ReSpefo.reset();
		
		MeasureRVDialog dialog = new MeasureRVDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			LOGGER.log(Level.FINER, "Measure RV dialog returned false.");
			return;
		}
		
		rvms = new RVMeasurements();
		results = new RVResults();
		index = 0;
		summary = false;
		
		String fileName = dialog.getSpectrum();
		String[] measurements = dialog.getMeasurements();
		String[] corrections = dialog.getCorrections();
		
		try {
			spectrum = Spectrum.createFromFile(fileName);
		} catch (SpefoException exception) {
			LOGGER.log(Level.WARNING, "Couldn't import file.", exception);
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			mb.setMessage("Couldn't import file.\n\nDebug message:\n" + exception.getMessage());
			mb.open();
			return;
		}
		
		ySeries = spectrum.getYSeries();
		xSeries = spectrum.getXSeries();
		
		for (int i = 0; i < ySeries.length; i++) {
			if (Double.isNaN(ySeries[i])) {
				ySeries[i] = 1;
			}
		}
		
		deltaRV = ((xSeries[1] - xSeries[0]) * SPEED_OF_LIGHT) / (xSeries[0] * 3);
		
		double[] newXSeries = new double[xSeries.length * 3 - 2];
		newXSeries[0] = xSeries[0];
		for (int i = 1; i < newXSeries.length; i++) {
			newXSeries[i] = newXSeries[i - 1] * (1 + deltaRV / SPEED_OF_LIGHT);
			if (newXSeries[i] > xSeries[xSeries.length - 1]) {
				newXSeries = Arrays.copyOf(newXSeries, i);
				break;
			}
		}
	
		double[] newYSeries = Util.intep(xSeries, ySeries, newXSeries);
		xSeries = newXSeries;
		ySeries = newYSeries;
	
		rvms.loadMeasurements(measurements, false);
		rvms.loadMeasurements(corrections, true);
		rvms.removeInvalidMeasurements(xSeries);
		
		if (rvms.getNonCorrectionsCount() == 0) {
			LOGGER.log(Level.WARNING, "Nothing to measure in the main .stl file(s).");
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setMessage("Nothing to measure in the main .stl file(s).");
			mb.open();
			return;
		}
		
		ReSpefo.setFilterPath(Paths.get(fileName).getParent().toString());
		
		SashForm sashForm = new SashForm(ReSpefo.getScene(), SWT.HORIZONTAL);
		sashForm.setLayout(new FillLayout());
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		container = new Composite(sashForm, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		
		createChart(rvms.getAt(index));
		
		Composite sideBar = new Composite(sashForm, SWT.NONE);
		layout = new GridLayout(1, true);
		sideBar.setLayout(layout);
		
		SashForm sashForm2 = new SashForm(sideBar, SWT.VERTICAL);
		sashForm2.setLayout(new FillLayout());
		sashForm2.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		list = new List(sashForm2, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		list2 = new List(sashForm2, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		
		list.setItems(rvms.getNames());
		list.setSelection(0);
		list.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				handle();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handle();
			}
			
			private void handle() {
				if (list.getSelectionIndex() != -1) {
					index = list.getSelectionIndex();
					list2.setSelection(-1);
				
					createChart(rvms.getAt(index));
				}
			}
		});
		list.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				ReSpefo.getScene().setFocus();
			}
		});
		

		list2.setItems(new String[0]);
		list2.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				handle();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handle();
			}
			
			private void handle() {
				if (list2.getSelectionIndex() != -1) {
					RVResult result = results.getAt(list2.getSelectionIndex());
					
					index = result.index;
					list.setSelection(index);
					createChart(rvms.getAt(index));
					
					move(result.shift);
				}
			}
		});
		list2.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				ReSpefo.getScene().setFocus();
			}
		});
		
		Button b = new Button(sideBar, SWT.PUSH | SWT.CENTER);
		b.setText("Finish");
		b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				finish();
			}
		});
		
		sashForm.setWeights(new int[]{85, 15});
		sashForm2.setWeights(new int[]{50, 50});
		
		ReSpefo.getScene().addSavedMouseWheelListener(new MouseWheelZoomListener(true, false));
		
		MeasureRVKeyListener keyListener = new MeasureRVKeyListener();
		ReSpefo.getScene().addSavedKeyListener(keyListener);
		container.addKeyListener(keyListener);
		
		ReSpefo.getScene().layout();
	}
	
	private void createChart(RVMeasurement rvm) {
		double[] origXSeries = xSeries;
		double[] tempXSeries = Util.fillArray(ySeries.length, 1, 1);

		double mid = Util.intep(origXSeries, tempXSeries, new double[] { rvm.l0 })[0];

		double[] mirroredYSeries = Util.trimArray(ySeries, xSeries, rvm.l0 - rvm.radius, rvm.l0 + rvm.radius);
		Util.mirrorArray(mirroredYSeries);

		int j = Arrays.binarySearch(origXSeries, Util.trimArray(xSeries, rvm.l0 - rvm.radius, rvm.l0 + rvm.radius)[0]);
		double[] temp = Util.fillArray(mirroredYSeries.length, j, 1);

		double[] mirroredXSeries = new double[temp.length];
		for (int i = 0; i < temp.length; i++) {
			mirroredXSeries[i] = 2 * mid - temp[temp.length - 1 - i];
		}

		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		chart = new ChartBuilder(container).setTitle(rvm.name + " #" + (index + 1) + " (" + rvm.l0 + ")")
				.setXAxisLabel("index").setYAxisLabel("relative flux I(λ)")
				.addSeries(LineStyle.SOLID, "original", ChartBuilder.GREEN, tempXSeries, ySeries)
				.addSeries(LineStyle.SOLID, "mirrored", ChartBuilder.BLUE, mirroredXSeries, mirroredYSeries)
				.adjustRange(1).build();
		ReSpefo.setChart(chart);
		chart.getAxisSet().zoomOut();

		MeasureRVMouseDragListener dragListener = new MeasureRVMouseDragListener();
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);

		shift = 0;
		summary = false;
	}
	
	private void createSummaryChart() {
		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		
		double[] newXSeries = new double[rvms.getCount()];
		for (int i = 0; i < newXSeries.length; i++) {
			newXSeries[i] = rvms.getAt(i).l0;
		}
		double newYSeries[] = Util.intep(xSeries, ySeries, newXSeries);
		
		chart = new ChartBuilder(container).setTitle("Press ENTER to finish").setXAxisLabel("wavelength (Å)")
				.setYAxisLabel("relative flux I(λ)")
				.addSeries(LineStyle.SOLID, "original", ChartBuilder.GREEN, xSeries, ySeries)
				.addSeries(LineStyle.NONE, "measurements", ChartBuilder.PINK, newXSeries, newYSeries).adjustRange()
				.build();
		ReSpefo.setChart(chart);
		
		summary = true;
	}
	
	private double getStep() {
		Chart chart = ReSpefo.getChart();
		ILineSeries ser = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
		IAxis XAxis = chart.getAxisSet().getXAxis(ser.getXAxisId());
	
		return (XAxis.getRange().upper - XAxis.getRange().lower) / 1000;
	}
	
	public void moveRight() {
		if (!summary) {
			moveRight(getStep());
		}
	}
	
	public void moveLeft() {
		if (!summary) {
			moveLeft(getStep());
		}
	}
	
	public void moveRight(double value) {
		if (!summary) {
			move(value);
		}
	}
	
	public void moveLeft(double value) {
		if (!summary) {
			move(-value);
		}
	}
	
	public void move(double value) {
		if (!summary) {
			shift += value;
				
			Chart chart = ReSpefo.getChart();
			ILineSeries ser = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
			ser.setXSeries(Util.adjustArrayValues(ser.getXSeries(), value));
				
			chart.redraw();
		}
	}
	
	public void enter() {
		if (summary) {
			finish();
		} else {
			if (list2.getSelectionIndex() == -1) {
				save();
			} else {
				// edit result
				RVResult result = results.getAt(list2.getSelectionIndex());
				MeasurementInputDialog dialog = new MeasurementInputDialog(ReSpefo.getShell(), result.category, result.comment, true);
				
				String category = dialog.open();
				if (category != null) {
					result.category = category;
					result.comment = dialog.getComment();
					result.rV = deltaRV * (shift / 2);
					result.shift = shift;
					result.radius = rvms.getAt(index).radius;
					
					list2.setItem(list2.getSelectionIndex(), result.toString());
					list2.setSelection(-1);
					
					createChart(rvms.getAt(index));
				}
			}
		}
	}
	
	public void insert() {
		if (!summary) {
			save();
		}
	}
	
	private void save() {
		RVMeasurement m = rvms.getAt(index);
		MeasurementInputDialog dialog;
		if (m.isCorrection) {
			dialog = new MeasurementInputDialog(ReSpefo.getShell(), "corr", false);
		} else {
			dialog = new MeasurementInputDialog(ReSpefo.getShell());
		}
		
		String category = dialog.open();
		if (category != null) {
			double l0 = m.l0;
			double rV = deltaRV * (shift / 2);
			double radius = m.radius;
			String name = m.name;
			String comment = dialog.getComment();

			RVResult rvresult = new RVResult(rV, shift, radius, category, l0, name, comment, index);
			results.addResult(rvresult);
			list2.add(rvresult.toString());

			if (list2.getSelectionIndex() != -1) {
				list2.setSelection(-1);
				createChart(rvms.getAt(index));
			} else {
				indexIncrement();
			}
		}
	}
	
	public void skip() {
		indexIncrement();
	}
	
	private void indexIncrement() {
		if (index < rvms.getCount() - 1) {
			index++;
			
			list.setSelection(index);
			
			createChart(rvms.getAt(index));
		} else {			
			createSummaryChart();
		}
	}
	
	public void delete() {
		if (list2.getSelectionIndex() != -1) {
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			mb.setMessage("Are you sure you want to delete this measurement?");
			if (mb.open() == SWT.YES) {
				results.remove(list2.getSelectionIndex());
				list2.remove(list2.getSelectionIndex());
				list2.setSelection(-1);
			}
		} else {
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_INFORMATION | SWT.OK);
			mb.setMessage("Select the measurement you want to delete.");
			mb.open();
		}
	}
	
	public void increaseRadius() {
		rvms.getAt(list.getSelectionIndex()).radius *= 1.5;
		createChart(rvms.getAt(index));
	}
	
	public void decreaseRadius() {
		rvms.getAt(list.getSelectionIndex()).radius /= 1.5;
		createChart(rvms.getAt(index));
	}
	
	private void finish() {
		RVResultsPrinter printer = new RVResultsPrinter(results);
		if (printer.printResults(spectrum)) {
			ReSpefo.reset();
			
			LOGGER.log(Level.INFO, "File was successfully saved.");
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_INFORMATION | SWT.OK);
			mb.setMessage("File was successfully saved.");
			mb.open();
		}
	}
	
}
