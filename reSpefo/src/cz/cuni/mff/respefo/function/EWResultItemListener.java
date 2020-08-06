package cz.cuni.mff.respefo.function;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.component.EWResultsFile;
import cz.cuni.mff.respefo.component.LstFile;
import cz.cuni.mff.respefo.component.LstFileRecord;
import cz.cuni.mff.respefo.dialog.EWResultDialog;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpefoException;

public class EWResultItemListener extends Function {
	private static EWResultItemListener instance;

	private EWResultItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new EWResultItemListener");
	}

	public static EWResultItemListener getInstance() {
		if (instance == null) {
			instance = new EWResultItemListener();
		}
		
		return instance;
	}


	@Override
	public void handle(SelectionEvent event) {
		EWResultDialog dialog = new EWResultDialog(ReSpefo.getShell());
		if (!dialog.open()) {
			return;
		}
		
		LstFile lstFile = dialog.getLstFile();
		String[] eqwFileNames = dialog.getEqwFileNames();				
		List<EWResultsFile> ewResults = new ArrayList<>();
		
		for (String eqwFileName : eqwFileNames) {
			try {
				if (eqwFileName != null) {
					ewResults.add(new EWResultsFile(eqwFileName));
				} else {
					ewResults.add(EWResultsFile.invalidFile());
				}
				
			} catch (SpefoException exception) {
				Message.error("Eqw file is invalid [" + eqwFileName +"].", exception);
				return;
			}
		}
		
		File ewResultsFile = new File(FileUtils.stripFileExtension(lstFile.getFileName()) + ".eqw");
		
		try {
			printToFile(ewResultsFile, lstFile, ewResults);
			
		} catch (SpefoException exception) {
			FileUtils.clearFileIfExists(ewResultsFile);
			Message.error("Couldn't create results file.", exception);
			return;
		}
		
		Message.fileSavedSuccessfuly();
	}

	private void printToFile(File file, LstFile lstFile, List<EWResultsFile> ewResults) throws SpefoException {
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println(FileUtils.stripParent(FileUtils.stripFileExtension(lstFile.getFileName())));
			
			Set<String> names = ewResults.stream().map(r -> r.isValid() ? r.getNames() : null).filter(Objects::nonNull).flatMap(Set::stream).sorted().collect(Collectors.toSet());
			
			for (String name : names) {
				writer.println();
				writer.println(name);
				writer.println(" Jul. date " + " " + "    EW    " + " " + "   FWHM   " + " " + "     V    " + " " + "     R    " + " " + "    Ic    " + " " + "    V/R   " + " " + "  (V+R)/2 " + " " + "    File  ");
				
				for (int i = 0; i < lstFile.recordsCount(); i++) {
					EWResultsFile resultsFile = ewResults.get(i);
					if (!resultsFile.isValid()) {
						continue;
					}
					
					Map<String, Double> map = resultsFile.forName(name);
					if (map == null) {
						continue;
					}
					
					LstFileRecord record = lstFile.getAt(i);
					
					writer.print(MathUtils.formatDouble(record.getJulianDate(), 5, 4, false));
					writer.print(" ");
					writer.print(MathUtils.formatDouble(map.getOrDefault("EW", 9999.9999), 4, 4));
					writer.print(" ");
					writer.print(MathUtils.formatDouble(map.getOrDefault("FWHM", 9999.9999), 4, 4));
					writer.print(" ");
					writer.print(MathUtils.formatDouble(map.getOrDefault("V", 9999.9999), 4, 4));
					writer.print(" ");
					writer.print(MathUtils.formatDouble(map.getOrDefault("R", 9999.9999), 4, 4));
					writer.print(" ");
					writer.print(MathUtils.formatDouble(map.getOrDefault("Ic", 9999.9999), 4, 4));
					writer.print(" ");
					writer.print(MathUtils.formatDouble(vToR(map), 4, 4));
					writer.print(" ");
					writer.print(MathUtils.formatDouble(vRAvg(map), 4, 4));
					writer.print(" ");
					writer.print(record.getFileName() == null ? "" : record.getFileName());
					writer.println();
				}
			}
			
			if (writer.checkError()) {
				throw new SpefoException("PrintWriter encountered and error.");
			}
		} catch (IOException e) {
			throw new SpefoException(e.getMessage());
		}
	}
	
	private double vToR(Map<String, Double> map) {
		Double v = map.get("V");
		Double r = map.get("R");
		
		if (v == null || r == null) {
			return 9999.9999;
		} else {
			return v / r;
		}
	}
	
	private double vRAvg(Map<String, Double> map) {
		Double v = map.get("V");
		Double r = map.get("R");
		
		if (v == null || r == null) {
			return 9999.9999;
		} else {
			return (v + r) / 2;
		}
	}
}
