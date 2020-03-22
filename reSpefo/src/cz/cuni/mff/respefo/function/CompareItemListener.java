package cz.cuni.mff.respefo.function;

import static cz.cuni.mff.respefo.util.FillLayoutBuilder.fillLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.dialog.CompareDialog;
import cz.cuni.mff.respefo.dialog.SelectValuesDialog;
import cz.cuni.mff.respefo.listeners.CompareKeyListener;
import cz.cuni.mff.respefo.listeners.CompareMouseDragListener;
import cz.cuni.mff.respefo.listeners.MouseDragListener;
import cz.cuni.mff.respefo.listeners.MouseWheelZoomListener;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.ChartBuilder;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Message;
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
	
	private Spectrum spectrumA, spectrumB;
	private double xShift, yScale;
	private Text xShiftText, yScaleText;
	
	@Override
	public void handle(SelectionEvent event) {

		CompareDialog dialog = new CompareDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			return;
		}
		
		String fileA = dialog.getFileA();
		String fileB = dialog.getFileB();
		
		try {
			 spectrumA = Spectrum.createFromFile(fileA);
			
		} catch (SpefoException e) {
			Message.error("Couldn't open file [" + fileA + "].", e);
			return;
		}
		
		try {
			 spectrumB = Spectrum.createFromFile(fileB);
			
		} catch (SpefoException e) {
			Message.error("Couldn't open file [" + fileB + "].", e);
			return;
		}
		
		ReSpefo.reset();
		
		xShift = 0;
		yScale = 1;
		
		createChartAndAddListeners(spectrumA, spectrumB);
		createBottomBar();
		
		ReSpefo.getScene().layout();
	}
	
	public void up() {
		yScale += 0.1;
		
		adjustYScale(true);
	}
	
	public void down() {
		yScale -= 0.1;
		
		adjustYScale(true);
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
		yScale = 1;
		
		adjustShift();
	}
	
	private void selectValues() {
		SelectValuesDialog dialog = new SelectValuesDialog(ReSpefo.getShell());
		
		if (dialog.open(xShiftText.getText(), yScaleText.getText())) {
			xShift = dialog.getXShift();
			yScale = dialog.getYScale();
			
			adjustShift();
		}
	}
	
	private void createChartAndAddListeners(Spectrum spectrumA, Spectrum spectrumB) {
		Chart chart = ChartBuilder.chart(ReSpefo.getScene())
				.setXAxisLabel("wavelength (Å)")
				.setYAxisLabel("relative flux I(λ)")
				.addLineSeries(LineStyle.SOLID, "seriesA", ChartBuilder.GREEN, spectrumA.getXSeries(), spectrumA.getYSeries())
				.addLineSeries(LineStyle.SOLID, "seriesB", ChartBuilder.BLUE, spectrumB.getXSeries(), spectrumB.getYSeries())
				.adjustRange()
				.build();
				
		setTitle(chart, spectrumA.getName(), spectrumB.getName());

		ReSpefo.setChart(chart);
		
		ReSpefo.getScene().addSavedKeyListener(new CompareKeyListener());
		ReSpefo.getScene().addSavedMouseWheelListener(new MouseWheelZoomListener());
		
		MouseDragListener dragListener = new CompareMouseDragListener(true);
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
	}
	
	private void setTitle(Chart chart, String titleA, String titleB) {
		ITitle title = chart.getTitle();
		title.setText(titleA + " x " + titleB); 
		title.setStyleRanges(new StyleRange[] {
				new StyleRange(0, titleA.length(), ChartBuilder.GREEN, null),
				new StyleRange(titleA.length() + 3, titleA.length() + 3 + titleB.length(), ChartBuilder.BLUE, null)});
		
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
		
		yScaleText = new Text(yShiftComposite, SWT.CENTER | SWT.READ_ONLY);
		yScaleText.setText("1.0");
		
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
		adjustYScale(false);
		
		ReSpefo.getChart().redraw();
		ReSpefo.getScene().forceFocus();
	}
	
	private void adjustXShift(boolean redraw) {
				
		ReSpefo.getChart().getSeriesSet().getSeries("seriesB")
			.setXSeries(adjustedXValues());
		
		xShiftText.setText(Double.toString(MathUtils.round(xShift, 3)));
		
		if (redraw) {
			ReSpefo.getChart().redraw();
			ReSpefo.getScene().forceFocus();
		}
	}
	
	private void adjustYScale(boolean redraw) {

		ReSpefo.getChart().getSeriesSet().getSeries("seriesB")
			.setYSeries(ArrayUtils.multiplyArrayElements(spectrumB.getYSeries(), yScale));

		yScaleText.setText(Double.toString(MathUtils.round(yScale, 3)));
		
		if (redraw) {
			ReSpefo.getChart().redraw();
			ReSpefo.getScene().forceFocus();
		}
	}
	
	private double[] adjustedXValues() {
		double[] xSeries = spectrumB.getXSeries();
		for (int i = 0; i < xSeries.length; i++) {
			xSeries[i] = xSeries[i] + xShift * xSeries[i] / MathUtils.SPEED_OF_LIGHT;
		}
		return xSeries;
	}

	private double getRelativeXStep() {
		Chart chart = ReSpefo.getChart();
		ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries("seriesB");
		IAxis XAxis = chart.getAxisSet().getXAxis(series.getXAxisId());
	
		return (XAxis.getRange().upper - XAxis.getRange().lower) / 100;
	}
}
