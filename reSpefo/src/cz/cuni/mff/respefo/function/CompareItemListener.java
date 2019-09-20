package cz.cuni.mff.respefo.function;

import static cz.cuni.mff.respefo.util.FillLayoutBuilder.fillLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.component.SeriesSet;
import cz.cuni.mff.respefo.dialog.SelectValuesDialog;
import cz.cuni.mff.respefo.listeners.CompareKeyListener;
import cz.cuni.mff.respefo.listeners.MouseDragListener;
import cz.cuni.mff.respefo.listeners.MouseWheelZoomListener;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.ChartBuilder;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpectrumUtils;
import cz.cuni.mff.respefo.util.SpefoException;

public class CompareItemListener extends Function {

	private static CompareItemListener instance = null;

	private CompareItemListener() {
	}

	public static CompareItemListener getInstance() {
		if (instance == null) {
			instance = new CompareItemListener();
		}
		
		return instance;
	}
	
	private SeriesSet spectrumASeries, spectrumBSeries;
	private double xShift, yShift;
	private Text xShiftText, yShiftText;
	
	@Override
	public void handle(SelectionEvent event) {
		String fileA = FileUtils.fileOpenDialog(FileType.SPECTRUM);
		if (fileA == null) {
			return;
		}
		
		Spectrum spectrumA = null;
		try {
			 spectrumA = Spectrum.createFromFile(fileA);
			
		} catch (SpefoException e) {
			Message.error("Couldn't open file.", e);
			return;
		}
		
		String fileB = FileUtils.fileOpenDialog(FileType.SPECTRUM);
		if (fileB == null) {
			return;
		}
		
		Spectrum spectrumB = null;
		try {
			 spectrumB = Spectrum.createFromFile(fileB);
			
		} catch (SpefoException e) {
			Message.error("Couldn't open file.", e);
			return;
		}
		
		ReSpefo.reset();
		
		spectrumASeries = SpectrumUtils.transformToEquidistant(spectrumA.getXSeries(), spectrumA.getYSeries());
		spectrumBSeries = SpectrumUtils.transformToEquidistant(spectrumB.getXSeries(), spectrumB.getYSeries());
		
		xShift = 0;
		yShift = 0;
		
		createChartAndAddListeners(spectrumA, spectrumB);
		createBottomBar();
		
		ReSpefo.getScene().layout();
	}
	
	public void up() {
		yShift += getRelativeYStep();
		
		adjustYShift(true);
	}
	
	public void down() {
		yShift -= getRelativeYStep();
		
		adjustYShift(true);
	}
	
	public void left() {
		xShift -= getRelativeXStep();
		
		adjustXShift(true);
	}
	
	public void right() {
		xShift += getRelativeXStep();
		
		adjustXShift(true);
	}
	
	public void reset() {
		xShift = 0;
		yShift = 0;
		
		adjustShift();
	}
	
	private void selectValues() {
		SelectValuesDialog dialog = new SelectValuesDialog(ReSpefo.getShell());
		
		if (dialog.open(xShiftText.getText(), yShiftText.getText())) {
			xShift = dialog.getXShift();
			yShift = dialog.getYShift();
			
			adjustShift();
		}
	}
	
	private void createChartAndAddListeners(Spectrum spectrumA, Spectrum spectrumB) {
		Chart chart = ChartBuilder.in(ReSpefo.getScene())
				.setTitle(spectrumA.getName() + " x " + spectrumB.getName())
				.setXAxisLabel("index")
				.setYAxisLabel("relative flux I(λ)")
				.addLineSeries(LineStyle.SOLID, "seriesA", ChartBuilder.GREEN, spectrumASeries.getXSeries(), spectrumASeries.getYSeries())
				.addLineSeries(LineStyle.SOLID, "seriesB", ChartBuilder.BLUE, spectrumBSeries.getXSeries(), spectrumBSeries.getYSeries())
				.adjustRange()
				.build();
		
		ReSpefo.setChart(chart);
		
		ReSpefo.getScene().addSavedKeyListener(new CompareKeyListener());
		ReSpefo.getScene().addSavedMouseWheelListener(new MouseWheelZoomListener());
		
		MouseDragListener dragListener = new MouseDragListener(true);
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
	}
	
	private void createBottomBar() {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(ReSpefo.getScene(), SWT.H_SCROLL);
		
		final Composite bottomBar = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(bottomBar);
		bottomBar.setLayout(fillLayout(SWT.HORIZONTAL).marginWidth(10).spacing(20).build());
		
		final Composite xShiftComposite = new Composite(bottomBar, SWT.NONE);
		xShiftComposite.setLayout(new FillLayout());
		
		final CLabel xShiftLabel = new CLabel(xShiftComposite, SWT.CENTER);
		xShiftLabel.setText("X shift: ");
		
		final Button decrementXShiftButton = new Button(xShiftComposite, SWT.PUSH);
		decrementXShiftButton.setText("-");
		decrementXShiftButton.addListener(SWT.Selection, e -> left());
		
		xShiftText = new Text(xShiftComposite, SWT.CENTER | SWT.READ_ONLY);
		xShiftText.setText("0.0");
		
		final Button incrementXShiftButton = new Button(xShiftComposite, SWT.PUSH);
		incrementXShiftButton.setText("+");
		incrementXShiftButton.addListener(SWT.Selection, e -> right());
		
		
		final Composite yShiftComposite = new Composite(bottomBar, SWT.NONE);
		yShiftComposite.setLayout(new FillLayout());
		
		final CLabel yShiftLabel = new CLabel(yShiftComposite, SWT.CENTER);
		yShiftLabel.setText("Y shift: ");
		
		final Button decrementYShiftButton = new Button(yShiftComposite, SWT.PUSH);
		decrementYShiftButton.setText("-");
		decrementYShiftButton.addListener(SWT.Selection, e -> down());
		
		yShiftText = new Text(yShiftComposite, SWT.CENTER | SWT.READ_ONLY);
		yShiftText.setText("0.0");
		
		final Button incrementYShiftButton = new Button(yShiftComposite, SWT.PUSH);
		incrementYShiftButton.setText("+");
		incrementYShiftButton.addListener(SWT.Selection, e -> up());
		
		final Button resetButton = new Button(bottomBar, SWT.PUSH);
		resetButton.setText("Reset");
		resetButton.addListener(SWT.Selection, e -> reset());
		
		final Button setValuesButton = new Button(bottomBar, SWT.PUSH);
		setValuesButton.setText("Set Values");
		setValuesButton.addListener(SWT.Selection, e -> selectValues());
		
		bottomBar.setSize(bottomBar.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	}
	
	private void adjustShift() {
		adjustXShift(false);
		adjustYShift(false);
		
		ReSpefo.getChart().redraw();
		ReSpefo.getScene().forceFocus();
	}
	
	private void adjustXShift(boolean redraw) {
				
		ReSpefo.getChart().getSeriesSet().getSeries("seriesB")
			.setXSeries(ArrayUtils.addValueToArrayElements(spectrumBSeries.getXSeries(), xShift));
		
		xShiftText.setText(Double.toString(MathUtils.round(xShift, 3)));
		
		if (redraw) {
			ReSpefo.getChart().redraw();
			ReSpefo.getScene().forceFocus();
		}
	}
	
	private void adjustYShift(boolean redraw) {

		ReSpefo.getChart().getSeriesSet().getSeries("seriesB")
			.setYSeries(ArrayUtils.addValueToArrayElements(spectrumBSeries.getYSeries(), yShift));

		yShiftText.setText(Double.toString(MathUtils.round(yShift, 3)));
		
		if (redraw) {
			ReSpefo.getChart().redraw();
			ReSpefo.getScene().forceFocus();
		}
	}

	private double getRelativeXStep() {
		Chart chart = ReSpefo.getChart();
		ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries("seriesB");
		IAxis XAxis = chart.getAxisSet().getXAxis(series.getXAxisId());
	
		return (XAxis.getRange().upper - XAxis.getRange().lower) / 1000;
	}
	
	private double getRelativeYStep() {
		Chart chart = ReSpefo.getChart();
		ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries("seriesB");
		IAxis YAxis = chart.getAxisSet().getYAxis(series.getYAxisId());
	
		return (YAxis.getRange().upper - YAxis.getRange().lower) / 1000;
	}
}
