package cz.cuni.mff.respefo.function;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.component.LstFile;
import cz.cuni.mff.respefo.component.LstFileRecord;
import cz.cuni.mff.respefo.dialog.AddToLstDialog;
import cz.cuni.mff.respefo.spectrum.FitsSpectrum;
import cz.cuni.mff.respefo.spectrum.OldSpefoSpectrum;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpefoException;

public class AddToLstItemListener extends Function {
	private static AddToLstItemListener instance;

	private AddToLstItemListener() {}

	public static AddToLstItemListener getInstance() {
		if (instance == null) {
			instance = new AddToLstItemListener();
		}
		return instance;
	}

	
	@Override
	public void handle(SelectionEvent event) {
		AddToLstDialog dialog = new AddToLstDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			return;
		}
		
		String lstFileName = dialog.getLstFileName();
		String[] spectrumFileNames = dialog.getFileNames();
		boolean includeFileName = dialog.includeFileName();
		String filePrefix = dialog.getFilePrefix();
		
		LstFile lstFile;
		try {
			lstFile = new LstFile(lstFileName);
		} catch (SpefoException exception) {
			Message.error("Couldn't open file [" + lstFileName + "]", exception);
			return;
		}
		
		for (String spectrumFileName : spectrumFileNames) {
			try {
				Spectrum spectrum = Spectrum.createFromFile(spectrumFileName);
				int index = lstFile.recordsCount() + 1;
				
				lstFile.addRecord(convertSpectrumToRecord(spectrum, index, includeFileName));
				
				String extension = FileUtils.getFileExtension(spectrumFileName);
				String fileName = Paths.get(lstFile.getFileName()).getParent().toString() + File.separator + filePrefix
						+ String.format("%05d", index) + (!extension.isEmpty() ? "." + extension : "");
				
				FileUtils.renameFile(spectrumFileName, fileName);
							
			} catch (SpefoException exception) {
				
				LOGGER.log(Level.WARNING, "Error while processing spectrum [" + spectrumFileName + "]", exception);
			} catch (IOException exception) {
				
				LOGGER.log(Level.WARNING, "Couldn't rename file [" + spectrumFileName + "]", exception);
			}
		}
		
		try {
			lstFile.save();
			
		} catch (SpefoException exception) {
			
			Message.couldNotSaveFile(exception);
			return;
		}
		
		Message.fileSavedSuccessfuly();
	}

	private LstFileRecord convertSpectrumToRecord(Spectrum spectrum, int index, boolean includeFileName) {
		double expTime = 0.0;
		double julianDate = 0.0;
		double rvCorr = 0.0;
		String date = "0000 00 00 00 00 00";
		
		if (spectrum instanceof FitsSpectrum) {
			FitsSpectrum fitsSpectrum = (FitsSpectrum) spectrum;
			
			expTime = fitsSpectrum.getExpTime();
			julianDate = Double.isNaN(fitsSpectrum.getJulianDate()) ? 0.0 : fitsSpectrum.getJulianDate();
			date = fitsSpectrum.getLstDate();
			if (fitsSpectrum.getRvCorrection() != null) {
				rvCorr = fitsSpectrum.getRvCorrection().getValue();
			}
		} else if (spectrum instanceof OldSpefoSpectrum) {
			OldSpefoSpectrum oldSpefoSpectrum = (OldSpefoSpectrum) spectrum;
			
			rvCorr = oldSpefoSpectrum.getRvCorr();
		}
		
		return new LstFileRecord(index, expTime, julianDate, rvCorr, date, includeFileName ? spectrum.getFileName() : null);
	}
}
