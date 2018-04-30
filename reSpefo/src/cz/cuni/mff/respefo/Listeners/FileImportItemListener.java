package cz.cuni.mff.respefo.Listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Listener;
import org.swtchart.Chart;
import org.swtchart.LineStyle;
import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
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

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}
}
