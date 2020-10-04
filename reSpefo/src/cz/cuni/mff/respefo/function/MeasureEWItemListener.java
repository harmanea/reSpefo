package cz.cuni.mff.respefo.function;

import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.swtchart.Chart;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.component.EWResultsPrinter;
import cz.cuni.mff.respefo.component.Measurement;
import cz.cuni.mff.respefo.component.MeasurementResults;
import cz.cuni.mff.respefo.component.Measurements;
import cz.cuni.mff.respefo.dialog.CategoryDialog;
import cz.cuni.mff.respefo.dialog.MeasureEWDialog;
import cz.cuni.mff.respefo.listeners.MeasureEWKeyListener;
import cz.cuni.mff.respefo.listeners.MeasureEWMouseDragListener;
import cz.cuni.mff.respefo.listeners.MouseWheelZoomListener;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.ChartBuilder;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpefoException;

public class MeasureEWItemListener extends Function {
	private static MeasureEWItemListener instance;
	private MeasureEWItemListener() {}

	public static MeasureEWItemListener getInstance() {
		if (instance == null) {
			instance = new MeasureEWItemListener();
		}
		
		return instance;
	}
	
	private Spectrum spectrum;
	
	private Measurements measurements;
	private MeasurementResults results;
	private int index;
	private int activeLine;
	
	private boolean moving;
	private double shift;
	
	private Composite container;
	private List topList, bottomList;
	private Button buttonLeft, buttonRight;
	
	@Override
	public void handle(SelectionEvent event) {
		MeasureEWDialog dialog = new MeasureEWDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			LOGGER.log(Level.FINER, "Measure Intensity dialog returned false.");
			return;
		}
		
		measurements = new Measurements();
		index = 0;
		activeLine = -2;
		
		moving = false;
		shift = 0;
		
		String fileName = dialog.getFileName();
		String[] items = dialog.getItems();
		
		try {
			spectrum = Spectrum.createFromFile(fileName);
		} catch (SpefoException exception) {
			Message.error("Couldn't import file.", exception);
			return;
		}
		ReSpefo.reset();
		
		measurements.loadMeasurements(items, false);
		measurements.removeInvalidMeasurements(spectrum.getXSeries());
		
		if (measurements.getCount() == 0) {
			Message.warning("There are no valid measurements.");
			return;
		}
		
		results = new MeasurementResults(measurements, spectrum.getXSeries());
		
		createWorkspace();
		createChart(measurements.getAt(0));
		ReSpefo.getScene().layout();
	}

	private void createWorkspace() {
		SashForm sash = new SashForm(ReSpefo.getScene(), SWT.HORIZONTAL);
		sash.setLayout(new FillLayout());
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		container = new Composite(sash, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		
		Composite sideBar = new Composite(sash, SWT.NONE);
		layout = new GridLayout(1, true);
		sideBar.setLayout(layout);
		
		topList = new List(sideBar, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		topList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		topList.setItems(measurements.getNames());
		topList.setSelection(0);
		topList.addListener(SWT.Selection, selectionEvent -> selectMeasurement());
		topList.addListener(SWT.FocusIn, focusEvent -> ReSpefo.getScene().forceFocus());
		
		Composite leftRightComposite = new Composite(sideBar, SWT.NONE);
		leftRightComposite.setLayout(new GridLayout(2, true));
		leftRightComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		buttonLeft = new Button(leftRightComposite, SWT.TOGGLE | SWT.CENTER);
		buttonLeft.setText("Left");
		buttonLeft.setLayoutData(new GridData(GridData.FILL_BOTH));
		buttonLeft.setEnabled(false);
		buttonLeft.addListener(SWT.Selection, event -> setActiveLine(-2));
		
		buttonRight = new Button(leftRightComposite, SWT.TOGGLE | SWT.CENTER);
		buttonRight.setText("Right");
		buttonRight.setLayoutData(new GridData(GridData.FILL_BOTH));
		buttonRight.addListener(SWT.Selection, event -> setActiveLine(-1));
		
		bottomList = new List(sideBar, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		bottomList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		bottomList.addListener(SWT.Selection, selectionEvent -> setActiveLine(bottomList.getSelectionIndex()));
		bottomList.addListener(SWT.FocusIn, focusEvent -> ReSpefo.getScene().forceFocus());
		
		Composite plusMinusComposite = new Composite(sideBar, SWT.NONE);
		plusMinusComposite.setLayout(new GridLayout(2, true));
		plusMinusComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button buttonPlus = new Button(plusMinusComposite, SWT.PUSH | SWT.CENTER);
		buttonPlus.setText("+");
		buttonPlus.setLayoutData(new GridData(GridData.FILL_BOTH));
		buttonPlus.addListener(SWT.Selection, event -> add());
		
		Button buttonMinus = new Button(plusMinusComposite, SWT.PUSH | SWT.CENTER);
		buttonMinus.setText("-");
		buttonMinus.setLayoutData(new GridData(GridData.FILL_BOTH));
		buttonMinus.addListener(SWT.Selection, event -> remove());
		
		Button buttonFinish = new Button(sideBar, SWT.PUSH | SWT.CENTER);
		buttonFinish.setText("Finish");
		buttonFinish.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonFinish.addListener(SWT.Selection, selectionEvent -> finish());
		
		sash.setWeights(new int[]{85, 15});
		
		ReSpefo.getScene().addSavedMouseWheelListener(new MouseWheelZoomListener(true, false));
		
		MeasureEWKeyListener keyListener = new MeasureEWKeyListener();
		ReSpefo.getScene().addSavedKeyListener(keyListener);
		container.addKeyListener(keyListener);
	}
	
	private void createChart(Measurement measurement) {
		Chart chart = ReSpefo.getChart();

		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		chart = ChartBuilder.chart(container).setTitle(measurement.getName() + " #" + (index + 1) + " (" + measurement.getL0() + ")")
				.setXAxisLabel("wavelength (Å)").setYAxisLabel("relative flux I(λ)")
				.addLineSeries(LineStyle.SOLID, "original", ChartBuilder.GREEN, spectrum.getXSeries(), spectrum.getYSeries())
				.build();
		ReSpefo.setChart(chart);
		
		adjustView();
		
		chart.getPlotArea().addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent event) {			
				Range range = ReSpefo.getChart().getAxisSet().getXAxis(0).getRange();
				double diff = range.upper - range.lower;
				
				int xLeft = (int) (event.width * (spectrum.getX(results.get(index).getLeft()) - range.lower) / diff);
				if (activeLine == -2) {
					event.gc.setForeground(ChartBuilder.CYAN);
					if (moving) {
						xLeft = (int) (event.width * (spectrum.getX(results.get(index).getLeft()) + shift - range.lower) / diff);
					}
				} else {
					event.gc.setForeground(ChartBuilder.BLUE);
				}
				event.gc.drawLine(xLeft, 0, xLeft, event.height);
				
				int xRight = (int) (event.width * (spectrum.getX(results.get(index).getRight()) - range.lower) / diff);
				if (activeLine == -1) {
					event.gc.setForeground(ChartBuilder.CYAN);
					if (moving) {
						xRight = (int) (event.width * (spectrum.getX(results.get(index).getRight()) + shift - range.lower) / diff);
					}
				} else {
					event.gc.setForeground(ChartBuilder.BLUE);
				}
				event.gc.drawLine(xRight, 0, xRight, event.height);
				
				for (int i = 0; i < results.get(index).size(); i++) {
					double point = spectrum.getX(results.get(index).getPoints().get(i));
					int x = (int) (event.width * (point - range.lower) / diff);
					
					if (activeLine == i) {
						event.gc.setForeground(ChartBuilder.ORANGE);
						if (moving) {
							x = (int) (event.width * (point + shift - range.lower) / diff);
						}
					} else {
						event.gc.setForeground(ChartBuilder.GRAY);
					}
					event.gc.drawLine(x, 0, x, event.height);
				}
			}
		});
		
		MeasureEWMouseDragListener dragListener = new MeasureEWMouseDragListener();
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
	}
	
	public void adjustView() {
		Chart chart = ReSpefo.getChart();
		
		double left = spectrum.getX(results.get(index).getLeft());
		double right = spectrum.getX(results.get(index).getRight());
		
		chart.getAxisSet().getXAxis(0).setRange(new Range(left, right));
		
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (int i = 0; i < spectrum.getSize(); i++) {
			double x = spectrum.getX(i);
			if (x >= left) {
				if (x <= right) {
					double y = spectrum.getY(i);
					
					if (y < min) {
						min = y;
					}
					if (y > max) {
						max = y;
					}
					
				} else {
					break;
				}
			}
		}
		chart.getAxisSet().getYAxis(0).setRange(new Range(min - MathUtils.DOUBLE_PRECISION, max + MathUtils.DOUBLE_PRECISION));
		
		chart.getAxisSet().zoomOut();
	}
	
	private void setActiveLine(int index) {
		boolean leftButton = index == -2;
		boolean rightButton = index == -1;
		
		buttonLeft.setEnabled(!leftButton);
		buttonLeft.setSelection(leftButton);
		
		buttonRight.setEnabled(!rightButton);
		buttonRight.setSelection(rightButton);
		
		if (index < 0) {
			bottomList.setSelection(-1);
		}
		
		activeLine = index;
		ReSpefo.getChart().redraw();
		container.forceFocus();
	}
	
	private void selectMeasurement() {
		if (topList.getSelectionIndex() != -1 && topList.getSelectionIndex() != index) {
			index = topList.getSelectionIndex();
			setActiveLine(-2);
		
			bottomList.setItems(results.get(index).getCategories().stream().toArray(String[]::new));
			
			createChart(measurements.getAt(index));
		}
	}
	
	public void skip() {
		if (index < measurements.getCount() - 1) {
			topList.setSelection(index + 1);
			selectMeasurement();
		} else {
			finish();
		}
	}
	
	public void add() {
		String category = new CategoryDialog(ReSpefo.getShell()).open();
		if (category != null) {
			Range range = ReSpefo.getChart().getAxisSet().getXAxis(0).getRange();
			double newPoint = (range.upper + range.lower) / 2;
			
			results.get(index).getPoints().add(ArrayUtils.findClosest(spectrum.getXSeries(), newPoint));
			results.get(index).getCategories().add(category);
			
			bottomList.add(category);
			bottomList.setSelection(results.get(index).size() - 1);
			
			setActiveLine(results.get(index).size() - 1);
		}
	}
	
	public void remove() {
		if (activeLine >= 0 && activeLine < results.get(index).getPoints().size()) {
			results.get(index).getPoints().remove(activeLine);
			results.get(index).getCategories().remove(activeLine);
			
			bottomList.remove(activeLine);
			
			if (activeLine > 0) {
				setActiveLine(activeLine - 1);
			} else if (results.get(index).getPoints().isEmpty()) {
				setActiveLine(-2);
			} else {
				ReSpefo.getChart().redraw();
			}
		}
	}
	
	public void moveLeft() {
		move(-1);
	}
	
	public void moveRight() {
		move(1);
	}
	
	public void move(int shift) {
		if (activeLine == -2) {
			results.get(index).moveLeft(shift);
		} else if (activeLine == -1) {
			results.get(index).moveRight(shift);
		} else {
			results.get(index).getPoints().set(activeLine, results.get(index).getPoints().get(activeLine) + shift);
		}
		ReSpefo.getChart().redraw();
	}
	
	public void startMoving() {
		moving = true;
	}
	
	public void stopMoving() {
		if (activeLine == -2) {
			double x = spectrum.getX(results.get(index).getLeft()) + shift;
			int newIndex = ArrayUtils.findClosest(spectrum.getXSeries(), x);
			results.get(index).setLeft(newIndex);
			
		} else if (activeLine == -1) {
			double x = spectrum.getX(results.get(index).getRight()) + shift;
			int newIndex = ArrayUtils.findClosest(spectrum.getXSeries(), x);
			results.get(index).setRight(newIndex);
			
		} else {
			double x = spectrum.getX(results.get(index).getPoints().get(activeLine)) + shift;
			int newIndex = ArrayUtils.findClosest(spectrum.getXSeries(), x);				
			results.get(index).getPoints().set(activeLine, newIndex);
		}
		
		shift = 0;
		ReSpefo.getChart().redraw();
	}
	
	public void updateShift(double shift) {
		this.shift += shift;
		ReSpefo.getChart().redraw();
	}
	
	private void finish() {
		if (Message.question("Are you sure you want to finish?") && EWResultsPrinter.printResults(spectrum, measurements, results)) {
			ReSpefo.reset();
			Message.fileSavedSuccessfuly();
		}
	}
}
