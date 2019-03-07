package cz.cuni.mff.respefo.rvResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.measureRV.RVResults;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Util;

public class RVResultItemListener implements SelectionListener {
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
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "RV Result widget default selected");
		
		handle(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		LOGGER.log(Level.FINEST, "RV Result widget selected");
		
		handle(event);
	}

	private void handle(SelectionEvent event) {
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
		
		String[] categories = Arrays.stream(rvResultsList).filter(Objects::nonNull).map(r -> r.getCategories())
				.flatMap(Stream::of).distinct().sorted().toArray(String[]::new);
		
		File file = new File(lstFile.getFileName().substring(0, lstFile.getFileName().lastIndexOf('.')) + ".rvs");
		
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println(lstFile.getHeader());
			writer.println(lstFile.getInfo());
			writer.println();
			
			writer.print("  N.\tJul. date \t corr \t");
			for (String category : categories) {
				writer.print(category + "\t");
			}
			writer.println();
			
			for (int i = 0; i < lstFile.recordsCount(); i++) {
				LstFileRecord record = lstFile.getAt(i);
				RVResults results = rvResultsList[i];
				
				writer.print(Util.formatDouble(record.getIndex(), 3, 0, false) + "\t");
				writer.print(Util.formatDouble(record.getJulianDate(), 5, 4, false) + "\t");
				writer.print(Util.formatDouble(record.getRvCorr(), 2, 2, true));
				
				if (results == null) {
					continue;
				}
				
				for (String category : categories) {
					double rv = results.getRvOfCategory(category);
					
					if (Double.isNaN(rv)) {
						writer.print("\t9999.99"); // TODO make this dynamic
					} else {
						if (results.getRvCorr().isUndefined()) {
							rv += record.getRvCorr();
						}
						writer.print("\t" + Util.formatDouble(rv, 3, 2, true));
					}
				}
				writer.println();
			}
		} catch (FileNotFoundException exception) {
			Message.error("Couldn't write into file [" + file.getName() +"].", exception);
			return;
		}
		
		Message.info("File was succesfully written.");
		return;
	}
}
