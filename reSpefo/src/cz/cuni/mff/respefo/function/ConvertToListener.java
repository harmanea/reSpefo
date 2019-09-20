package cz.cuni.mff.respefo.function;

import static cz.cuni.mff.respefo.util.FileType.SPECTRUM;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.FileUtils;
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
		List<String> fileNames = FileUtils.multipleFilesDialog(SPECTRUM);
		
		loadSpectra(fileNames).forEach(spectrum ->
			spectrum.exportToFits(FileUtils.stripFileExtension(spectrum.getFileName()) + ".fits"));
	}
	
	public void convertToAscii() {
		List<String> fileNames = FileUtils.multipleFilesDialog(SPECTRUM);
		
		loadSpectra(fileNames).forEach(spectrum ->
			spectrum.exportToAscii(FileUtils.stripFileExtension(spectrum.getFileName()) + ".ascii"));
	}
	
	private List<Spectrum> loadSpectra(List<String> fileNames) {
		return fileNames.stream()
			.map(this::convertToSpectrum)
			.filter(object -> !Objects.isNull(object))
			.collect(Collectors.toList());
	}
	
	private Spectrum convertToSpectrum(String fileName) {
		try {
			return Spectrum.createFromFile(fileName);
		} catch (SpefoException exception) {
			LOGGER.log(Level.WARNING, "Couldn't convert file [" + fileName + "]", exception);
			
			return null;
		}
	}
}
