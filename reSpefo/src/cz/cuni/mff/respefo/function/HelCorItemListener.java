package cz.cuni.mff.respefo.function;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.component.LstFile;
import cz.cuni.mff.respefo.component.LstFileRecord;
import cz.cuni.mff.respefo.component.RvCorrection;
import cz.cuni.mff.respefo.spectrum.FitsSpectrum;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpefoException;

public class HelCorItemListener extends Function {	
	private static HelCorItemListener instance;
	private HelCorItemListener() {}

	public static HelCorItemListener getInstance() {
		if (instance == null) {
			instance = new HelCorItemListener();
		}
		
		return instance;
	}

	private static final String[] FITS_EXTENSIONS = new String[]{ "fits", "fit", "fts" };
	
	@Override
	public void handle(SelectionEvent event) {
		String lstFileName = FileUtils.fileOpenDialog(FileType.LST);
		if (lstFileName == null) {
			return;
		}
		
		LstFile lstFile = null;
		try {
			lstFile = new LstFile(lstFileName);
		} catch (SpefoException e) {
			Message.error("Couldn't open lst file.", e);
			return;
		}

		
		for (LstFileRecord record : lstFile.getRecords()) {
			String fileNameWithoutExtension = FileUtils.stripFileExtension(lstFile.getFileName()) + String.format("%05d", record.getIndex());
			String fileName = findFileName(fileNameWithoutExtension);
			
			if (fileName == null) {
				LOGGER.log(Level.WARNING, "File " + fileNameWithoutExtension + " was not found and will be skipped.");
				continue;
			}
			
			try {
				FitsSpectrum spectrum = new FitsSpectrum(fileName);
				
				double rvCorr = Optional.ofNullable(spectrum.getRvCorrection())
						.orElse(new RvCorrection(RvCorrection.UNDEFINED, 0)).getValue();
				
				if (!MathUtils.doublesEqual(record.getRvCorr(), rvCorr)) {
					spectrum.applyRvCorrection(record.getRvCorr() - rvCorr);
					spectrum.setRvCorrection(record.getRvCorr());
					
					if (!spectrum.exportToFits(fileName)) {
						throw new SpefoException("Couldn't save file.");
					}
				}
			} catch (Exception exception) {
				Message.error("Couldn't apply correction to file " + fileName, exception);
			}
		}
	}

	private String findFileName(String fileNameWithoutExtension) {
		for (String extension : FITS_EXTENSIONS) {
			String fileName = fileNameWithoutExtension + "." + extension;
			
			File file = new File(fileName);
			if (file.exists()) {
				return file.getPath();
			}
		}
		
		return null;
	}

}
