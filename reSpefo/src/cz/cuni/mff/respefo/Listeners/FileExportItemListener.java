package cz.cuni.mff.respefo.Listeners;

import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.util.Util;

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
			LOGGER.log(Level.WARNING, "Nothing to export");
			MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			messageBox.setMessage("No file loaded, nothing to export.");
			messageBox.open();
			return;
		}
		
		export(spectrum);
	}

	
	public boolean export(Spectrum spectrum) {
		String fileName = Util.openFileDialog(Util.SPECTRUM_SAVE);
		
		if (fileName == null) {
			LOGGER.log(Level.FINER, "File dialog returned null");
			return false;
		} else if (Paths.get(fileName).getParent() != null) {
			ReSpefo.setFilterPath(Paths.get(fileName).getParent().toString());
		}
		
		File destFile = new File(fileName);
		if (destFile.exists()) {
			MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox.setMessage("File already exists. Would you like to overwrite it?");
			if (messageBox.open() != SWT.YES) {
				LOGGER.log(Level.FINER, "Overwrite dialog didn't return yes");
				return false;
			}
		}
		
		String extension = Util.getFileExtension(fileName);
		
		// TODO remove code repetition
		switch (extension) {
		case "":
		case "txt":
		case "ascii":
			if (spectrum.exportToAscii(fileName)) {
				LOGGER.log(Level.INFO, "File was successfully saved");
				MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_INFORMATION | SWT.OK);
				messageBox.setMessage("File was successfully saved.");
				messageBox.open();
			} else {
				LOGGER.log(Level.WARNING, "Couldn't save file");
				MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
				messageBox.setMessage("Couldn't save file.");
				messageBox.open();
				return false;
			}
			break;
		case "fits":
		case "fit":
		case "fts":
			if (spectrum.exportToFits(fileName)) {
				LOGGER.log(Level.INFO, "File was successfully saved");
				MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_INFORMATION | SWT.OK);
				messageBox.setMessage("File was successfully saved.");
				messageBox.open();
			} else {
				LOGGER.log(Level.WARNING, "Couldn't save file");
				MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
				messageBox.setMessage("Couldn't save file.");
				messageBox.open();
				return false;
			}
			break;
		case "rui":
		case "uui":
		case "rci":
		case "rfi":
			LOGGER.log(Level.WARNING, "Old Spefo formats aren't supported yet");
			MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			messageBox.setMessage("Old Spefo formats aren't supported yet.");
			messageBox.open();
			return false;
		default:
			LOGGER.log(Level.WARNING, "Not a supported file format");
			messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			messageBox.setMessage("Not a supported file format.");
			messageBox.open();
			return false;
		}
		
		return true;
	}
}
