package cz.cuni.mff.respefo.Listeners;

import java.awt.MouseInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.swtchart.Chart;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.Range;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpectrumBuilder;
import cz.cuni.mff.respefo.Util;

public class FileImportItemListener implements SelectionListener {

	public FileImportItemListener() {
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		Spectrum spectrum = Util.importSpectrum();

		if (spectrum == null) {
			return;
		}

		for (Listener l : ReSpefo.getShell().getListeners(SWT.KeyDown)) {
			ReSpefo.getShell().removeListener(SWT.KeyDown, l);
		}

		ReSpefo.setSpectrum(spectrum);

		Chart chart = ReSpefo.getChart();

		if (chart != null) {
			chart.dispose();
		}
		
		chart = new ChartBuilder(ReSpefo.getShell()).setTitle(spectrum.name()).setXAxisLabel("wavelength (Å)").setYAxisLabel("relative flux")
				.addSeries(LineStyle.SOLID, "series", ChartBuilder.green, spectrum.getXSeries(), spectrum.getYSeries()).adjustRange().pack();
		
		ReSpefo.setChart(chart);

		ReSpefo.getShell().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				Chart chart = ReSpefo.getChart();

				switch (e.keyCode) {
				case 'w':
				case SWT.ARROW_UP: // arrow up
					chart.getAxisSet().getYAxis(0).scrollUp();
					break;
				case 'a':
				case SWT.ARROW_LEFT: // arrow left
					chart.getAxisSet().getXAxis(0).scrollDown();
					break;
				case 's':
				case SWT.ARROW_DOWN: // arrow down
					chart.getAxisSet().getYAxis(0).scrollDown();
					break;
				case 'd':
				case SWT.ARROW_RIGHT: // arrow right
					chart.getAxisSet().getXAxis(0).scrollUp();
					break;
				case SWT.SPACE: // space
					chart.getAxisSet().adjustRange();
					break;
				case '+': // +
				case 16777259:
				case 49:
					chart.getAxisSet().zoomIn();
					break;
				case '-': // -
				case 16777261:
				case 47:
					chart.getAxisSet().zoomOut();
					break;
				case SWT.KEYPAD_8: // NumPad up
					chart.getAxisSet().getYAxis(0).zoomIn();
					break;
				case SWT.KEYPAD_2: // NumPad down
					chart.getAxisSet().getYAxis(0).zoomOut();
					break;
				case SWT.KEYPAD_4: // NumPad left
					chart.getAxisSet().getXAxis(0).zoomOut();
					break;
				case SWT.KEYPAD_6: // NumPad right
					chart.getAxisSet().getXAxis(0).zoomIn();
					break;
				}
				chart.redraw();
			}
		});

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}
}
