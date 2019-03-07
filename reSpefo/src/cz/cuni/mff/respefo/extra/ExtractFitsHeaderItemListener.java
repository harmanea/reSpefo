package cz.cuni.mff.respefo.extra;

import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import cz.cuni.mff.respefo.FitsSpectrum;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Util;

public class ExtractFitsHeaderItemListener implements SelectionListener {
	private static ExtractFitsHeaderItemListener instance;
	private ExtractFitsHeaderItemListener() {}
	
	public static ExtractFitsHeaderItemListener getInstance() {
		if (instance == null) {
			instance = new ExtractFitsHeaderItemListener();
		}
		
		return instance;
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		handle(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		handle(event);
	}

	private void handle(SelectionEvent event) {
		try {
			String fileName = Util.openFileDialog(Util.SPECTRUM_LOAD);
			
			FitsSpectrum spectrum = (FitsSpectrum) Spectrum.createFromFile(fileName);
			
			String headerFile = fileName.substring(0, fileName.lastIndexOf('.')) + ".header";
			try (PrintStream ps = new PrintStream(headerFile)) {
				spectrum.getHeader().dumpHeader(ps);
				
				if (ps.checkError()) {
					throw new IOException("PrintStream encountered an error.");
				}
			}
			
			Message.info(ReSpefo.getShell(), "Header extracted successfully");
		} catch (ClassCastException classCastException) {
			Message.error(ReSpefo.getShell(), "Spectrum is not in FITS format.", classCastException);
			
		} catch (SpefoException spefoException) {
			Message.error(ReSpefo.getShell(), "Error while opening file.", spefoException);
			
		} catch (IOException ioException) {
			Message.error(ReSpefo.getShell(), "Error while writing to file.", ioException);
			
		} catch (Exception exception) {
			Message.error(ReSpefo.getShell(), "Internal error.", exception);
		}
	}
}
