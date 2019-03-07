package cz.cuni.mff.respefo.Listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.swtchart.Chart;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.util.ChartBuilder;
import cz.cuni.mff.respefo.util.Util;

public class FileImportItemListener implements SelectionListener {
	private static FileImportItemListener instance;
	
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	private FileImportItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new FileImportItemListener");
	}
	
	public static FileImportItemListener getInstance() {
		if (instance == null) {
			instance = new FileImportItemListener();
		}
		
		return instance;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "Import file widget selected");
		handle(event);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "Import file widget default selected");
		handle(event);
	}
	
	private void handle(SelectionEvent event) {	
		String fileName = Util.openFileDialog(Util.SPECTRUM_LOAD);

		if (fileName == null) {
			LOGGER.log(Level.FINER, "File dialog returned null");
			return;
		}
		
		Spectrum spectrum;
		
		try {
			spectrum = Spectrum.createFromFile(fileName);
		} catch (SpefoException e) {
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			mb.setMessage("Couldn't import file.\n\nDebug message:\n" + e.getMessage());
			mb.open();
			return;
		}
		
		ReSpefo.reset();

		ReSpefo.setSpectrum(spectrum);
		
		Chart chart = ChartBuilder.from(ReSpefo.getScene()).setTitle(spectrum.getName()).setXAxisLabel("wavelength (Å)").setYAxisLabel("relative flux I(λ)")
				.addLineSeries(LineStyle.SOLID, "series", ChartBuilder.GREEN, spectrum.getXSeries(), spectrum.getYSeries()).adjustRange().build();
		
		ReSpefo.setChart(chart);

		ReSpefo.getScene().addSavedKeyListener(new DefaultKeyListener());
		ReSpefo.getScene().addSavedMouseWheelListener(new MouseWheelZoomListener());
		
		MouseDragListener dragListener = new MouseDragListener(true);
		chart.getPlotArea().addMouseListener(dragListener);
		chart.getPlotArea().addMouseMoveListener(dragListener);
		
		SelectionBoxListener boxListener = new SelectionBoxListener();
		chart.getPlotArea().addMouseListener(boxListener);
        chart.getPlotArea().addMouseMoveListener(boxListener);
        chart.getPlotArea().addPaintListener(boxListener);
		
        // Proof of concept, might get removed
        Label label = new Label(ReSpefo.getScene(), SWT.RIGHT);
        label.setText("0,0");
        label.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        chart.getPlotArea().addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent e) {
				Chart chart = ReSpefo.getChart();
				
				Rectangle bounds = chart.getPlotArea().getBounds();
				
				Range chartXRange = chart.getAxisSet().getXAxis(0).getRange();
				double realX = chartXRange.lower + ((chartXRange.upper - chartXRange.lower) * ((double)e.x / bounds.width));
				
				Range chartYRange = chart.getAxisSet().getYAxis(0).getRange();
				double realY = chartYRange.upper - ((chartYRange.upper - chartYRange.lower) * ((double)e.y / bounds.height));
				
				label.setText("x: " + (double)Math.round(realX * 100) / 100 + ", y: " + (double)Math.round(realY * 100) / 100);
			}
		});
        
        ReSpefo.getScene().layout();
	}
	
}
