package cz.cuni.mff.respefo.extra;

import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.FitsSpectrum;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.Listeners.AbstractSelectionListener;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;

public class ExtractFitsHeaderItemListener extends AbstractSelectionListener {
	private static ExtractFitsHeaderItemListener instance;
	private ExtractFitsHeaderItemListener() {}
	
	public static ExtractFitsHeaderItemListener getInstance() {
		if (instance == null) {
			instance = new ExtractFitsHeaderItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		try {
			String fileName = FileUtils.fileOpenDialog(FileType.SPECTRUM);
			
			FitsSpectrum spectrum = (FitsSpectrum) Spectrum.createFromFile(fileName);
			
			String headerFile = fileName.substring(0, fileName.lastIndexOf('.')) + ".header";
			try (PrintStream ps = new PrintStream(headerFile)) {
				spectrum.getHeader().dumpHeader(ps);
				
				if (ps.checkError()) {
					throw new IOException("PrintStream encountered an error.");
				}
			}
			
			Message.info("Header extracted successfully");
		} catch (ClassCastException classCastException) {
			Message.error("Spectrum is not in FITS format.", classCastException);
			
		} catch (SpefoException spefoException) {
			Message.error("Error while opening file.", spefoException);
			
		} catch (IOException ioException) {
			Message.error("Error while writing to file.", ioException);
			
		} catch (Exception exception) {
			Message.error("Internal error.", exception);
		}
	}
}
