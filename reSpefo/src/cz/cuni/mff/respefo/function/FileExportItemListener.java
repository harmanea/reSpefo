package cz.cuni.mff.respefo.function;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;

public class FileExportItemListener extends Function {
	private static FileExportItemListener instance;
	
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	private FileExportItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new FileExportItemListener");
	}
	
	public static FileExportItemListener getInstance() {
		if (instance == null) {
			instance = new FileExportItemListener();
		}
		
		return instance;
	}
	
	public void handle(SelectionEvent event) {
		Spectrum spectrum = ReSpefo.getSpectrum();
		
		if (spectrum == null) { 
			Message.warning("No file loaded, nothing to export.");
			return;
		}
		
		export(spectrum);
	}

	
	public boolean export(Spectrum spectrum) {
		String fileName = FileUtils.fileSaveDialog(FileType.SPECTRUM);
		
		if (fileName == null) {
			return false;
		}
		
		File destFile = new File(fileName);
		if (destFile.exists()) {
			if (!Message.question("File already exists. Would you like to overwrite it?")) {
				return false;
			}
		}
		
		String extension = FileUtils.getFileExtension(fileName);
		switch (extension) {
		case "":
		case "txt":
		case "ascii":
			if (spectrum.exportToAscii(fileName)) {
				Message.fileSavedSuccessfuly();
			} else {
				Message.warning("Couldn't save file.");
				return false;
			}
			break;
		case "fits":
		case "fit":
		case "fts":
			if (spectrum.exportToFits(fileName)) {
				Message.fileSavedSuccessfuly();
			} else {
				Message.warning("Couldn't save file.");
				return false;
			}
			break;
		case "rui":
		case "uui":
		case "rci":
		case "rfi":
			Message.warning("Old Spefo formats aren't supported.");
			return false;
		default:
			Message.warning("Not a supported file format");
			return false;
		}
		
		return true;
	}
}
