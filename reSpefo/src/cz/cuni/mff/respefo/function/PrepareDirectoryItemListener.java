package cz.cuni.mff.respefo.function;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.component.LstFile;
import cz.cuni.mff.respefo.component.LstFileRecord;
import cz.cuni.mff.respefo.dialog.PrepareDirectoryDialog;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpefoException;

public class PrepareDirectoryItemListener extends Function {
	private static PrepareDirectoryItemListener instance;
	private PrepareDirectoryItemListener() {}
	
	public static PrepareDirectoryItemListener getInstance() {
		if (instance == null) {
			instance = new PrepareDirectoryItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		PrepareDirectoryDialog dialog = new PrepareDirectoryDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			return;
		}
		
		String lstFileName = dialog.getLstFile();
		String projectPrefix = dialog.getProjectPrefix();
		
		LstFile lstFile;
		try {
			lstFile = new LstFile(lstFileName);
		} catch (SpefoException exception) {
			Message.error("Couldn't open lst file.", exception);
			return;
		}
		
		for (LstFileRecord record : lstFile.getRecords()) {
			String oldFileName = FileUtils.getFilterPath() + File.separator + record.getFileName();
			
			String extension = FileUtils.getFileExtension(oldFileName);
			String newFileName = FileUtils.getFilterPath() + File.separator + projectPrefix +String.format("%05d", record.getIndex()) + (extension != null ? "." + extension : "");
			try {
				FileUtils.renameFile(oldFileName, newFileName);
			} catch (IOException exception) {
				LOGGER.log(Level.WARNING, "Couldn't rename file " + oldFileName, exception);
			}
		}
		
		String oldLstFileName = lstFile.getFileName();
		String newLstFileName = Paths.get(oldLstFileName).getParent().toString() + File.separator + projectPrefix + ".lst";
		try {
			FileUtils.renameFile(oldLstFileName, newLstFileName);
		} catch (IOException exception) {
			LOGGER.log(Level.WARNING, "Couldn't rename lst file", exception);
			return;
		}
		
		Message.info("Files renamed successfuly");
	}
}
