package cz.cuni.mff.respefo.function;

import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.dialog.ConvertToDialog;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpefoException;

public class ConvertToListener {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	private static ConvertToListener instance;

	private ConvertToListener() {
	}

	public static ConvertToListener getInstance() {
		if (instance == null) {
			instance = new ConvertToListener();
		}
		return instance;
	}

	public void convertToFits() {
		convert("fits", (spectrum, fileName) -> spectrum.exportToFits(fileName));
	}
	
	public void convertToAscii() {
		convert("asc", (spectrum, fileName) -> spectrum.exportToAscii(fileName));
	}
	
	private void convert(String defaultFileExtension, BiFunction<Spectrum, String, Boolean> exportFunction) {
		ConvertToDialog dialog = new ConvertToDialog(ReSpefo.getShell());
		if (!dialog.open(defaultFileExtension)){
			return;
		}
		
		String fileExtension = dialog.getFileExtension();
		String[] fileNames = dialog.getFileNames();
		
		int skipped = 0;
		
		for (String fileName : fileNames) {
			Spectrum spectrum;
			try {
				spectrum = Spectrum.createFromFile(fileName);
			} catch (SpefoException exception) {
				skipped++;
				LOGGER.log(Level.WARNING, "Couldn't import file [" + fileName + "]", exception);
				continue;
			}
			
			if (!exportFunction.apply(spectrum, FileUtils.stripFileExtension(fileName) + "." + fileExtension)) {
				skipped++;
				LOGGER.log(Level.WARNING, "Couldn't export file [" + fileName + "]");
			}
		}
		
		Message.info("Conversion completed:\n\n" + (fileNames.length - skipped) + " files converted\n" + skipped + " files skipped");
	}
}
