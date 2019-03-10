package cz.cuni.mff.respefo.Listeners;

import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;

public class FileExportItemListener implements SelectionListener {
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
	
	@Override
	public void widgetSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "Export file widget selected");
		handle(event);
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "Import file default widget selected");
		handle(event);
	}
	
	private void handle(SelectionEvent event) {
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
			LOGGER.log(Level.FINER, "File dialog returned null");
			return false;
		} else if (Paths.get(fileName).getParent() != null) {
			ReSpefo.setFilterPath(Paths.get(fileName).getParent().toString());
		}
		
		File destFile = new File(fileName);
		if (destFile.exists()) {
			if (Message.question("File already exists. Would you like to overwrite it?") != SWT.YES) {
				
				LOGGER.log(Level.FINER, "Overwrite dialog didn't return yes");
				return false;
			}
		}
		
		String extension = FileUtils.getFileExtension(fileName);
		
		// TODO remove code repetition
		switch (extension) {
		case "":
		case "txt":
		case "ascii":
			if (spectrum.exportToAscii(fileName)) {
				Message.info("File was successfully saved.");
			} else {
				Message.warning("Couldn't save file.");
				return false;
			}
			break;
		case "fits":
		case "fit":
		case "fts":
			if (spectrum.exportToFits(fileName)) {
				Message.info("File was successfully saved.");
			} else {
				Message.warning("Couldn't save file.");
				return false;
			}
			break;
		case "rui":
		case "uui":
		case "rci":
		case "rfi":
			Message.warning("Old Spefo formats aren't supported yet.");
			return false;
		default:
			Message.warning("Not a supported file format");
			return false;
		}
		
		return true;
	}
}
