package cz.cuni.mff.respefo.function;

import java.util.Arrays;
import java.util.logging.Level;

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

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.component.Measurement;
import cz.cuni.mff.respefo.component.Measurements;
import cz.cuni.mff.respefo.component.RVResult;
import cz.cuni.mff.respefo.component.RVResults;
import cz.cuni.mff.respefo.component.RVResultsPrinter;
import cz.cuni.mff.respefo.component.RvCorrection;
import cz.cuni.mff.respefo.component.SeriesSet;
import cz.cuni.mff.respefo.dialog.MeasureRVDialog;
import cz.cuni.mff.respefo.dialog.MeasurementInputDialog;
import cz.cuni.mff.respefo.dialog.RVStepDialog;
import cz.cuni.mff.respefo.dialog.RvCorrDialog;
import cz.cuni.mff.respefo.listeners.MeasureRVKeyListener;
import cz.cuni.mff.respefo.listeners.MeasureRVMouseDragListener;
import cz.cuni.mff.respefo.listeners.MeasureRVMouseWheelZoomListener;
import cz.cuni.mff.respefo.spectrum.FitsSpectrum;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.ChartBuilder;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpectrumUtils;
import cz.cuni.mff.respefo.util.SpefoException;

public class MeasureRVItemListener extends Function {
	private static MeasureRVItemListener instance;
	
	private Measurements rvMeasurements;
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
		
		MeasureRVDialog dialog = new MeasureRVDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			LOGGER.log(Level.FINER, "Measure RV dialog returned false.");
			return;
		}
		
		rvMeasurements = new Measurements();
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
		ReSpefo.reset();
		
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
		
		deltaRV = ((xSeries[1] - xSeries[0]) * MathUtils.SPEED_OF_LIGHT) / (xSeries[0] * 3);
		
		SeriesSet newSeries = SpectrumUtils.transformToEquidistant(xSeries, ySeries);
		
		xSeries = newSeries.getXSeries();
		ySeries = newSeries.getYSeries();
	
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
		listOne.addListener(SWT.FocusIn, focusEvent -> ReSpefo.getScene().forceFocus());

		listTwo.setItems(new String[0]);
		listTwo.addListener(SWT.Selection, selectionEvent -> selectResult());
		listTwo.addListener(SWT.FocusIn, focusEvent -> ReSpefo.getScene().forceFocus());
		
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

	private void createChart(Measurement rvMeasurement) {
		double[] origXSeries = xSeries;
		double[] tempXSeries = ArrayUtils.fillArray(ySeries.length, 0, 1);

		double mid;
		
		int index = Arrays.binarySearch(origXSeries, rvMeasurement.getL0());
		if (index < 0) {
			index = - index - 1;
			double low = origXSeries[index - 1];
			double high = origXSeries[index];
			double lowNew = tempXSeries[index - 1];
			
			mid = lowNew + ((rvMeasurement.getL0() - low) / (high - low));
		} else {
			mid = tempXSeries[index];
		}

		double[] mirroredYSeries = ArrayUtils.mirrorArray(ArrayUtils.trimArray(ySeries, xSeries,
				rvMeasurement.getL0() - rvMeasurement.getRadius(), rvMeasurement.getL0() + rvMeasurement.getRadius()));

		int j = Arrays.binarySearch(origXSeries, ArrayUtils.trimArray(xSeries, rvMeasurement.getL0() - rvMeasurement.getRadius(),
				rvMeasurement.getL0() + rvMeasurement.getRadius())[0]);
		double[] temp = ArrayUtils.fillArray(mirroredYSeries.length, j, 1);

		double[] mirroredXSeries = new double[temp.length];
		for (int i = 0; i < temp.length; i++) {
			mirroredXSeries[i] = 2 * mid - temp[temp.length - 1 - i];
		}

		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		chart = ChartBuilder.chart(container).setTitle(rvMeasurement.getName() + " #" + (index + 1) + " (" + rvMeasurement.getL0() + ")")
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
			newXSeries[i] = rvMeasurements.getAt(i).getL0();
		}
		double newYSeries[] = MathUtils.intep(xSeries, ySeries, newXSeries);
		
		chart = ChartBuilder.chart(container).setTitle("Press ENTER to finish").setXAxisLabel("index")
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
				MeasurementInputDialog dialog = new MeasurementInputDialog(ReSpefo.getShell(), result.getCategory(), result.getComment(), true);
				
				String category = dialog.open();
				if (category != null) {
					result.setCategory(category);
					result.setComment(dialog.getComment());
					result.setrV(deltaRV * (shift / 2));
					result.setShift(shift);
					result.setRadius(rvMeasurements.getAt(index).getRadius());
										
					listTwo.setItem(listTwo.getSelectionIndex(), result.toString());
					listTwo.setSelection(-1);
					
					createChart(rvMeasurements.getAt(index));
				}
				ReSpefo.getScene().forceFocus();
			}
		}
	}
	
	public void insert() {
		if (!summary) {
			save();
		}
	}
	
	private void save() {
		Measurement rvMeasurement = rvMeasurements.getAt(index);
		MeasurementInputDialog dialog;
		if (rvMeasurement.isCorrection()) {
			dialog = new MeasurementInputDialog(ReSpefo.getShell(), "corr", false);
		} else {
			dialog = new MeasurementInputDialog(ReSpefo.getShell());
		}
		
		String category = dialog.open();
		if (category != null) {
			double l0 = rvMeasurement.getL0();
			double rV = deltaRV * (shift / 2);
			double radius = rvMeasurement.getRadius();
			String name = rvMeasurement.getName();
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
			if (Message.question("Are you sure you want to delete this measurement?")) {
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
			
			index = result.getIndex();
			listOne.setSelection(index);
			createChart(rvMeasurements.getAt(index));
			
			move(result.getShift());
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
		rvMeasurements.getAt(listOne.getSelectionIndex()).increaseRadius();
		createChart(rvMeasurements.getAt(index));
	}
	
	public void decreaseRadius() {
		rvMeasurements.getAt(listOne.getSelectionIndex()).decreaseRadius();
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
			Message.fileSavedSuccessfuly();
		}
	}
	
	private RvCorrection getRvCorrectionFromFitsHeader() {
		if (spectrum instanceof FitsSpectrum) {
			FitsSpectrum fitsSpectrum = (FitsSpectrum) spectrum;
			
			return fitsSpectrum.getRvCorrection();
		}
		
		return null;
	}
	
	private void applyCorrection(RvCorrection rvCorr) {
		xSeries = Arrays.stream(xSeries).map(value -> value + rvCorr.getValue()*(value / MathUtils.SPEED_OF_LIGHT)).toArray();
	}
}
