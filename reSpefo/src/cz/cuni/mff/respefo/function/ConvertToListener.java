package cz.cuni.mff.respefo.function;

import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.component.RVResults;
import cz.cuni.mff.respefo.dialog.ConvertToDialog;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.MathUtils;
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
		boolean adjustValues = dialog.adjustValues();
		
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
			
			if (adjustValues) {
				try {
					double deltaCorr = adjustValues(spectrum, fileName);
					spectrum.setName(spectrum.getName() + " with rv corr of " + Double.toString(deltaCorr));
				} catch (SpefoException exception) {
					skipped++;
					LOGGER.log(Level.WARNING, "Invalid .rvr file [" + fileName + "]", exception);
					continue;
				}
			}
			
			if (!exportFunction.apply(spectrum, FileUtils.stripFileExtension(fileName) + "." + fileExtension)) {
				skipped++;
				LOGGER.log(Level.WARNING, "Couldn't export file [" + fileName + "]");
			}
		}
		
		int converted = fileNames.length - skipped;
		Message.info("Conversion completed:\n\n" 
		+ converted + " file" + (converted != 1 ? "s" : "") + " converted\n"
		+ skipped + " file" + (skipped != 1 ? "s" : "") + " skipped");
	}
	
	private double adjustValues(Spectrum spectrum, String fileName) throws SpefoException {
		String rvrFileName = FileUtils.stripFileExtension(fileName) + ".rvr";
		RVResults rvResults = new RVResults(rvrFileName);
		
		double originalCorr = rvResults.getRvCorr().getValue();
		if (spectrum.getRvCorrection() != null && !MathUtils.doublesEqual(spectrum.getRvCorrection().getValue(), originalCorr)) {
			Message.warning("The RV corrections in the spectrum and .rvr files don't match!");
		}
		double measuredCorr = rvResults.getRvOfCategory("corr");
		if (Double.isNaN(measuredCorr)) {
			throw new SpefoException("rvr file does not contain corr measurements");
		}
		
		double deltaCorr = originalCorr - measuredCorr;
		
		double[] xSeries = spectrum.getXSeries();
		for (int i = 0; i < xSeries.length; i++) {
			xSeries[i] = xSeries[i] + deltaCorr * xSeries[i] / MathUtils.SPEED_OF_LIGHT;
		}
		spectrum.setXSeries(xSeries);
		
		return deltaCorr;
	}
}
