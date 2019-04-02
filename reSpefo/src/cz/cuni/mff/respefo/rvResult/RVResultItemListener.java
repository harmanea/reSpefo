package cz.cuni.mff.respefo.rvResult;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.listeners.AbstractSelectionListener;
import cz.cuni.mff.respefo.measureRV.RVResults;
import cz.cuni.mff.respefo.util.Message;

public class RVResultItemListener extends AbstractSelectionListener {
	private static RVResultItemListener instance;
	
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	private RVResultItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new RVResultItemListener");
	}
	
	public static RVResultItemListener getInstance() {
		if (instance == null) {
			instance = new RVResultItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		RVResultDialog dialog = new RVResultDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			return;
		}
		
		LstFile lstFile = dialog.getLstFile();
		String[] rvrFileNames = dialog.getRvFileNames();
		RVResults[] rvResultsList = new RVResults[rvrFileNames.length];
		
		for (int i = 0; i < rvrFileNames.length; i++) {
			try {
				if (rvrFileNames[i] != null) {
					rvResultsList[i] = new RVResults(rvrFileNames[i]);
				}
				
			} catch (SpefoException exception) {
				Message.error("Rvr file is invalid [" + rvrFileNames[i] +"].", exception);
				return;
			}
		}
		
		RVResultsTable table = new RVResultsTable(lstFile, rvResultsList);
		String fileNameWithoutSuffix = lstFile.getFileName().substring(0, lstFile.getFileName().lastIndexOf('.'));
		File rvsFile = new File(fileNameWithoutSuffix + ".rvs");
		File corFile = new File(fileNameWithoutSuffix + ".cor");
		
		try {
			table.printToRvsFile(rvsFile);
			
		} catch (SpefoException exception) {
			Message.error("Couldn't save rvs file.", exception);
			return;
		}
		
		try {
			table.printToCorFile(corFile);
			
		} catch (SpefoException exception) {
			Message.error("Couldn't save cor file.", exception);
			return;
		}
		
		Message.info("Files were successfully written.");
	}
}
