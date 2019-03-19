package cz.cuni.mff.respefo.measureRV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.LineStyle;

import cz.cuni.mff.respefo.FitsSpectrum;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.Listeners.AbstractSelectionListener;
import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.ChartBuilder;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Message;
import nom.tam.fits.Header;

public class MeasureRVItemListener extends AbstractSelectionListener {
	private static MeasureRVItemListener instance;
	
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	private static final double SPEED_OF_LIGHT = 299792.458;
	
	private RVMeasurements rvMeasurements;
	private RVResults results;
	private int index;
	private boolean summary;
	
	private Spectrum spectrum;
	private double[] xSeries;
	private double[] ySeries;
	private double deltaRV;
	
	private double shift;
	
	private Composite container; // holds current chart
	private List listOne;
	private List listTwo;
	private double rvStep;
	
	private Label rvStepLabel;
	private Button rvStepButton;

	private MeasureRVItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new MeasureRVItemListener");
	}
	
	public static MeasureRVItemListener getInstance() {
		if (instance == null) {
			instance = new MeasureRVItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		ReSpefo.reset();
		
		MeasureRVDialog dialog = new MeasureRVDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			LOGGER.log(Level.FINER, "Measure RV dialog returned false.");
			return;
		}
		
		rvMeasurements = new RVMeasurements();
		results = new RVResults();
		index = 0;
		summary = false;
		
		String fileName = dialog.getSpectrum();
		String[] measurements = dialog.getMeasurements();
		String[] corrections = dialog.getCorrections();
		rvStep = dialog.getRvStep();
		
		try {
			spectrum = Spectrum.createFromFile(fileName);
		} catch (SpefoException exception) {
			Message.error("Couldn't import file.", exception);
			return;
		}
		
		ySeries = spectrum.getYSeries();
		xSeries = spectrum.getXSeries();
		
		RvCorrection rvCorr = getRvCorrectionFromFitsHeader();
		if (rvCorr == null) {
			RvCorrDialog corrDialog = new RvCorrDialog(ReSpefo.getShell());
			if (corrDialog.open()) {
				rvCorr = corrDialog.getCorrection();
				if (corrDialog.applyCorrection()) {
					applyCorrection(rvCorr);
				}
			} else {
				rvCorr = new RvCorrection(RvCorrection.UNDEFINED, Double.NaN);
			}
		}
		results.setRvCorr(rvCorr);
		
		for (int i = 0; i < ySeries.length; i++) {
			if (Double.isNaN(ySeries[i])) {
				ySeries[i] = 1;
			}
		}
		
		deltaRV = ((xSeries[1] - xSeries[0]) * SPEED_OF_LIGHT) / (xSeries[0] * 3);
		
		ArrayList<Double> xList = new ArrayList<>();
		xList.add(xSeries[0]);
		while (xList.get(xList.size() - 1) < xSeries[xSeries.length - 1]) {
			xList.add(xList.get(xList.size() - 1) * (1 + deltaRV / SPEED_OF_LIGHT));
		}
		double[] newXSeries = xList.stream().mapToDouble(Double::doubleValue).toArray();
		double[] newYSeries = MathUtils.intep(xSeries, ySeries, newXSeries);
		xSeries = newXSeries;
		ySeries = newYSeries;
		
		/*
		if (!results.getRvCorr().isUndefined()) {
			xSeries = Util.adjustArrayValues(xSeries, results.getRvCorr().getValue() / (2*deltaRV));
		}
		*/
	
		rvMeasurements.loadMeasurements(measurements, false);
		rvMeasurements.loadMeasurements(corrections, true);
		rvMeasurements.removeInvalidMeasurements(xSeries);
		
		if (rvMeasurements.getCount() == 0) {
			Message.warning("There are no valid measurements.");
			return;
		}
		
		SashForm sashFormOne = new SashForm(ReSpefo.getScene(), SWT.HORIZONTAL);
		sashFormOne.setLayout(new FillLayout());
		sashFormOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		container = new Composite(sashFormOne, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		
		Composite sideBar = new Composite(sashFormOne, SWT.NONE);
		layout = new GridLayout(1, true);
		sideBar.setLayout(layout);
		
		SashForm sashFormTwo = new SashForm(sideBar, SWT.VERTICAL);
		sashFormTwo.setLayout(new FillLayout());
		sashFormTwo.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		listOne = new List(sashFormTwo, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		listTwo = new List(sashFormTwo, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		
		listOne.setItems(rvMeasurements.getNames());
		listOne.setSelection(0);
		listOne.addListener(SWT.Selection, selectionEvent -> selectMeasurement());
		listOne.addListener(SWT.FocusIn, focusEvent -> ReSpefo.getScene().setFocus());

		listTwo.setItems(new String[0]);
		listTwo.addListener(SWT.Selection, selectionEvent -> selectResult());
		listTwo.addListener(SWT.FocusIn, focusEvent -> ReSpefo.getScene().setFocus());
		
		Group group = new Group(sideBar, SWT.RADIO);
		group.setText("RV step (km/s)");
        layout = new GridLayout(2, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		
        rvStepLabel = new Label(group, SWT.CENTER);
        rvStepLabel.setText(Double.toString(rvStep));
        rvStepLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        
        rvStepButton = new Button(group, SWT.PUSH | SWT.CENTER);
        rvStepButton.setText("...");
        rvStepButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));
        rvStepButton.addListener(SWT.Selection, selectionEvent -> openRvStepDialog());
		
		Button buttonFinish = new Button(sideBar, SWT.PUSH | SWT.CENTER);
		buttonFinish.setText("Finish");
		buttonFinish.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonFinish.addListener(SWT.Selection, selectionEvent -> finish());
		
		sashFormOne.setWeights(new int[]{85, 15});
		sashFormTwo.setWeights(new int[]{50, 50});
		
		ReSpefo.getScene().addSavedMouseWheelListener(new MeasureRVMouseWheelZoomListener(true, false));
		
		MeasureRVKeyListener keyListener = new MeasureRVKeyListener();
		ReSpefo.getScene().addSavedKeyListener(keyListener);
		container.addKeyListener(keyListener);
		
		createChart(rvMeasurements.getAt(index));
		
		ReSpefo.getScene().layout();
	}

	private void createChart(RVMeasurement rvMeasurement) {
		double[] origXSeries = xSeries;
		double[] tempXSeries = ArrayUtils.fillArray(ySeries.length, 0, 1);

		double mid;
		
		int index = Arrays.binarySearch(origXSeries, rvMeasurement.l0);
		if (index < 0) {
			index = - index - 1;
			double low = origXSeries[index - 1];
			double high = origXSeries[index];
			double lowNew = tempXSeries[index - 1];
			
			mid = lowNew + ((rvMeasurement.l0 - low) / (high - low));
		} else {
			mid = tempXSeries[index];
		}

		double[] mirroredYSeries = ArrayUtils.mirrorArray(ArrayUtils.trimArray(ySeries, xSeries,
				rvMeasurement.l0 - rvMeasurement.radius, rvMeasurement.l0 + rvMeasurement.radius));

		int j = Arrays.binarySearch(origXSeries, ArrayUtils.trimArray(xSeries, rvMeasurement.l0 - rvMeasurement.radius,
				rvMeasurement.l0 + rvMeasurement.radius)[0]);
		double[] temp = ArrayUtils.fillArray(mirroredYSeries.length, j, 1);

		double[] mirroredXSeries = new double[temp.length];
		for (int i = 0; i < temp.length; i++) {
			mirroredXSeries[i] = 2 * mid - temp[temp.length - 1 - i];
		}

		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		chart = ChartBuilder.in(container).setTitle(rvMeasurement.name + " #" + (index + 1) + " (" + rvMeasurement.l0 + ")")
				.setXAxisLabel("index").setYAxisLabel("relative flux I(λ)")
				.addLineSeries(LineStyle.SOLID, "original", ChartBuilder.GREEN, tempXSeries, ySeries)
				.addLineSeries(LineStyle.SOLID, "mirrored", ChartBuilder.BLUE, mirroredXSeries, mirroredYSeries)
				.adjustRange(1).build();
		ReSpefo.setChart(chart);
		chart.getAxisSet().zoomOut();

		MeasureRVMouseDragListener dragListener = new MeasureRVMouseDragListener();
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
		
		rvStepButton.setEnabled(true);
		adjustRVStepLabel();
		
		shift = 0;
		summary = false;
	}
	
	private void createSummaryChart() {
		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		
		double[] newXSeries = new double[rvMeasurements.getCount()];
		for (int i = 0; i < newXSeries.length; i++) {
			newXSeries[i] = rvMeasurements.getAt(i).l0;
		}
		double newYSeries[] = MathUtils.intep(xSeries, ySeries, newXSeries);
		
		chart = ChartBuilder.in(container).setTitle("Press ENTER to finish").setXAxisLabel("index")
				.setYAxisLabel("relative flux I(λ)")
				.addLineSeries(LineStyle.SOLID, "original", ChartBuilder.GREEN, xSeries, ySeries)
				.addScatterSeries(PlotSymbolType.CIRCLE, "measurements", ChartBuilder.PINK, newXSeries, newYSeries).adjustRange()
				.build();
		ReSpefo.setChart(chart);
		
		rvStepButton.setEnabled(false);
		
		summary = true;
	}
	
	private double getRelativeStep() {
		Chart chart = ReSpefo.getChart();
		ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
		IAxis XAxis = chart.getAxisSet().getXAxis(series.getXAxisId());
	
		return (XAxis.getRange().upper - XAxis.getRange().lower) / 1000;
	}
	
	public void moveRight() {
		if (!summary) {
			if (rvStep < 0) {
				moveRight(getRelativeStep());
			} else {
				moveRight(2 * rvStep / deltaRV);
			}
		}
	}
	
	public void moveLeft() {
		if (!summary) {
			if(rvStep < 0) {
				moveLeft(getRelativeStep());
			} else {
				moveLeft(2 * rvStep / deltaRV);
			}
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
			ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries("mirrored");
			series.setXSeries(ArrayUtils.addValueToArrayElements(series.getXSeries(), value));
				
			chart.redraw();
		}
	}
	
	public void enter() {
		if (summary) {
			finish();
		} else {
			if (listTwo.getSelectionIndex() == -1) {
				save();
			} else {
				// edit result
				RVResult result = results.getAt(listTwo.getSelectionIndex());
				MeasurementInputDialog dialog = new MeasurementInputDialog(ReSpefo.getShell(), result.category, result.comment, true);
				
				String category = dialog.open();
				if (category != null) {
					result.category = category;
					result.comment = dialog.getComment();
					result.rV = deltaRV * (shift / 2);
					result.shift = shift;
					result.radius = rvMeasurements.getAt(index).radius;
					
					listTwo.setItem(listTwo.getSelectionIndex(), result.toString());
					listTwo.setSelection(-1);
					
					createChart(rvMeasurements.getAt(index));
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
		RVMeasurement rvMeasurement = rvMeasurements.getAt(index);
		MeasurementInputDialog dialog;
		if (rvMeasurement.isCorrection) {
			dialog = new MeasurementInputDialog(ReSpefo.getShell(), "corr", false);
		} else {
			dialog = new MeasurementInputDialog(ReSpefo.getShell());
		}
		
		String category = dialog.open();
		if (category != null) {
			double l0 = rvMeasurement.l0;
			double rV = deltaRV * (shift / 2);
			double radius = rvMeasurement.radius;
			String name = rvMeasurement.name;
			String comment = dialog.getComment();

			RVResult rvresult = new RVResult(rV, shift, radius, category, l0, name, comment, index);
			results.addResult(rvresult);
			listTwo.add(rvresult.toString());

			if (listTwo.getSelectionIndex() != -1) {
				listTwo.setSelection(-1);
				createChart(rvMeasurements.getAt(index));
			}
		}
	}
	
	public void skip() {
		indexIncrement();
	}
	
	private void indexIncrement() {
		if (index < rvMeasurements.getCount() - 1) {
			index++;
			
			listOne.setSelection(index);
			listTwo.setSelection(-1);
			
			createChart(rvMeasurements.getAt(index));
		} else if (!summary) {			
			createSummaryChart();
		}
	}
	
	public void delete() {
		if (listTwo.getSelectionIndex() != -1) {
			if (Message.question("Are you sure you want to delete this measurement?") == SWT.YES) {
				results.remove(listTwo.getSelectionIndex());
				listTwo.remove(listTwo.getSelectionIndex());
				listTwo.setSelection(-1);
			}
		} else {
			Message.info("Select the measurement you want to delete.");
		}
	}
	
	private void selectMeasurement() {
		if (listOne.getSelectionIndex() != -1) {
			index = listOne.getSelectionIndex();
			listTwo.setSelection(-1);
		
			createChart(rvMeasurements.getAt(index));
		}
	}
	
	private void selectResult() {
		if (listTwo.getSelectionIndex() != -1) {
			RVResult result = results.getAt(listTwo.getSelectionIndex());
			
			index = result.index;
			listOne.setSelection(index);
			createChart(rvMeasurements.getAt(index));
			
			move(result.shift);
		}
	}
	
	private void openRvStepDialog() {
		RVStepDialog dialog = new RVStepDialog(ReSpefo.getShell(), rvStep);
		if (dialog.open()) {
			rvStep = dialog.getRVStep();
			
			if (rvStep < 0) {
				rvStepLabel.setText(Double.toString(MathUtils.round(getRelativeStep(), 2)));
			} else {
				rvStepLabel.setText(Double.toString(rvStep));
			}
		} else {
			LOGGER.log(Level.FINER, "RV step dialog returned false.");
		}
		
		ReSpefo.getScene().forceFocus();
	}
	
	public void increaseRadius() {
		rvMeasurements.getAt(listOne.getSelectionIndex()).radius *= 1.5;
		createChart(rvMeasurements.getAt(index));
	}
	
	public void decreaseRadius() {
		rvMeasurements.getAt(listOne.getSelectionIndex()).radius /= 1.5;
		createChart(rvMeasurements.getAt(index));
	}
	
	public void adjustRVStepLabel() {
		if (!summary) {
			if (rvStep < 0) {
				rvStepLabel.setText(Double.toString(MathUtils.round(getRelativeStep() * deltaRV / 2, 4)));
			} else {
				rvStepLabel.setText(Double.toString(rvStep));
			}
		}
	}
	
	private void finish() {
		RVResultsPrinter printer = new RVResultsPrinter(results);
		if (printer.printResults(spectrum)) {
			ReSpefo.reset();
			Message.info("File was successfully saved.");
		}
	}
	
	private RvCorrection getRvCorrectionFromFitsHeader() {
		if (spectrum instanceof FitsSpectrum) {
			FitsSpectrum fitsSpectrum = (FitsSpectrum) spectrum;
			Header header = fitsSpectrum.getHeader();
			
			if (header.containsKey("HJD") || header.containsKey("BJD")) {
				double rvCorr = header.getDoubleValue("VHELIO", Double.NaN);
				if (!Double.isNaN(rvCorr)) {
					return new RvCorrection(header.containsKey("HJD")
							? RvCorrection.HELIOCENTRIC
							: RvCorrection.BARYCENTRIC, rvCorr);
				}
			}
		}
		
		return null;
	}
	
	private void applyCorrection(RvCorrection rvCorr) {
		xSeries = Arrays.stream(xSeries).map(value -> value + rvCorr.getValue()*(value / SPEED_OF_LIGHT)).toArray();
	}
}
