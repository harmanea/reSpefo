package cz.cuni.mff.respefo.function;

import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.swtchart.Chart;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.listeners.DefaultKeyListener;
import cz.cuni.mff.respefo.listeners.MouseDragListener;
import cz.cuni.mff.respefo.listeners.MouseWheelZoomListener;
import cz.cuni.mff.respefo.listeners.SelectionBoxListener;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.ChartBuilder;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpefoException;

public class FileImportItemListener extends Function {
	private static FileImportItemListener instance;
	
	private FileImportItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new FileImportItemListener");
	}
	
	public static FileImportItemListener getInstance() {
		if (instance == null) {
			instance = new FileImportItemListener();
		}
		
		return instance;
	}
	
	public void handle(SelectionEvent event) {	
		
		String fileName = FileUtils.fileOpenDialog(FileType.SPECTRUM);

		if (fileName == null) {
			LOGGER.log(Level.FINER, "File dialog returned null");
			return;
		}
		
		Spectrum spectrum;
		
		try {
			spectrum = Spectrum.createFromFile(fileName);
		} catch (SpefoException exception) {
			
			Message.error("Couldn't import file", exception);
			return;
		}
		ReSpefo.reset();

		ReSpefo.setSpectrum(spectrum);
		
		Chart chart = ChartBuilder.chart(ReSpefo.getScene())
				.setTitle(spectrum.getName())
				.setXAxisLabel("wavelength (Å)")
				.setYAxisLabel("relative flux I(λ)")
				.addLineSeries(LineStyle.SOLID, "series", ChartBuilder.GREEN, spectrum.getXSeries(), spectrum.getYSeries())
				.adjustRange()
				.build();
		
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
