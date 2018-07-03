package cz.cuni.mff.respefo.Listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.Util;

public class FileImportItemListener implements SelectionListener {

	private boolean drag = false;
	private long startMillis;
	private int startX, startY;
	private int prevX, prevY;
	
	public FileImportItemListener() {
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		Spectrum spectrum = Util.importSpectrum();

		if (spectrum == null) {
			return;
		}

		Util.clearListeners();

		ReSpefo.setSpectrum(spectrum);

		Chart chart = ReSpefo.getChart();

		if (chart != null) {
			chart.dispose();
		}
		
		chart = new ChartBuilder(ReSpefo.getShell()).setTitle(spectrum.name()).setXAxisLabel("wavelength (Å)").setYAxisLabel("relative flux I(λ)")
				.addSeries(LineStyle.SOLID, "series", ChartBuilder.green, spectrum.getXSeries(), spectrum.getYSeries()).adjustRange().build();
		
		ReSpefo.setChart(chart);

		ReSpefo.getShell().addKeyListener(new DefaultMovementListener());
		
		chart.getPlotArea().addMouseListener(new MouseListener() {

			@Override
			public void mouseDown(MouseEvent e) {
				startMillis = System.currentTimeMillis();
				if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
					drag = true;
					
					startX = e.x;
					startY = e.y;
					
					prevX = e.x;
					prevY = e.y;
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				drag = false;
				if ((e.stateMask & SWT.CTRL) == SWT.CTRL && System.currentTimeMillis() - startMillis > 50) { // drag release
					Chart chart = ReSpefo.getChart();
					Range ChartXRange = chart.getAxisSet().getXAxis(0).getRange();
					Range ChartYRange = chart.getAxisSet().getYAxis(0).getRange();
					
					Rectangle bounds = chart.getPlotArea().getBounds();
					
					Range XRange = new Range(ChartXRange.lower + (ChartXRange.upper - ChartXRange.lower) * startX / bounds.width,
							ChartXRange.lower + (ChartXRange.upper - ChartXRange.lower) * e.x / bounds.width);
					Range YRange = new Range(ChartYRange.lower + (ChartYRange.upper - ChartYRange.lower) * (bounds.height - startY) / bounds.height,
							ChartYRange.lower + (ChartYRange.upper - ChartYRange.lower) * (bounds.height - e.y) / bounds.height);
					
					for (IAxis x : chart.getAxisSet().getXAxes()) {
						x.setRange(XRange);
					}
					for (IAxis y : chart.getAxisSet().getYAxes()) {
						y.setRange(YRange);
					}
				
					chart.redraw();
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
        
        chart.getPlotArea().addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent e) {
				if (drag) {
					prevX = e.x;
					prevY = e.y;
					ReSpefo.getChart().redraw();
				}
			}
		});
        
        chart.getPlotArea().addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				if (drag) {
					e.gc.drawRectangle(startX, startY, prevX - startX, prevY - startY);
				}
			}
		});
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}
}
