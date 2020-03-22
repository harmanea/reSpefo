package cz.cuni.mff.respefo.function;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.component.LstFile;
import cz.cuni.mff.respefo.component.LstFileRecord;
import cz.cuni.mff.respefo.component.RvCorrection;
import cz.cuni.mff.respefo.dialog.FitsToLstDialog;
import cz.cuni.mff.respefo.spectrum.FitsSpectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpefoException;
import nom.tam.fits.FitsException;

public class FitsToLstItemListener extends Function {
	private static FitsToLstItemListener instance;
	private FitsToLstItemListener() {}
	
	public static FitsToLstItemListener getInstance() {
		if (instance == null) {
			instance = new FitsToLstItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		FitsToLstDialog dialog = new FitsToLstDialog(ReSpefo.getShell());
		if(!dialog.open()) {
			return;
		}
		
		String directoryName = dialog.getDirectoryName();
		if (directoryName == null) {
			return;
		}
		boolean nestedDirectories = dialog.isNestedDirectories();
		String header = Optional.ofNullable(dialog.getHeader()).orElse("");
		for (long i = header.chars().filter(ch -> ch == '\n').count(); i < 3; ++i) {
			header += '\n';
		}
		
		try (Stream<Path> paths = Files.walk(Paths.get(directoryName), nestedDirectories ? Integer.MAX_VALUE : 1)) {
			LstFile file = paths.parallel()
					.filter(this::isFitsFile)
					.map(this::spectrumFromPath)
					.filter(Objects::nonNull)
					.map(this::lstFileRecordFromSpectrum)
					.sorted()
					.collect(Collector.of(() -> new LstFile(),
							(lstFile, lstFileRecord) -> ((LstFile) lstFile).addRecord(lstFileRecord),
							(firstLstFile, secondLstFile) -> {
								firstLstFile.addRecords(secondLstFile.getRecords());
								return firstLstFile;
							}));
				
			
			String fileName = directoryName + File.separator + Paths.get(directoryName).getFileName() + ".lst";
			file.setFileName(fileName);
			file.setHeader(header);
			
			for (int i = 0; i < file.recordsCount(); ++i) {
				file.getAt(i).setIndex(i + 1);
			}
			
			file.save();
			
			Message.fileSavedSuccessfuly();
		} catch (IOException exception) {
			Message.error("Error reading files.", exception);
		} catch (SpefoException exception) {
			Message.error("Error saving file.", exception);
		}
	}
	
	private boolean isFitsFile(Path path) {
		return Files.isRegularFile(path)
			&& (path.toString().endsWith(".fits") || path.toString().endsWith(".fts") || path.toString().endsWith(".fit"));
	}
	
	private FitsSpectrum spectrumFromPath(Path path) {
		try {
			return new FitsSpectrum(path.toString());
			
		} catch (SpefoException | FitsException exception) {
			LOGGER.log(Level.WARNING, "File [" + path + "] couldn't be loaded and was skipped.", exception);
			return null;
		}
	}
	
	private LstFileRecord lstFileRecordFromSpectrum(FitsSpectrum spectrum) {
		LstFileRecord result = new LstFileRecord();
		
		result.setExp(spectrum.getExpTime());
		result.setDate(spectrum.getLstDate());
		result.setFileName(spectrum.getFileName());
		result.setRvCorr(
				Optional.ofNullable(spectrum.getRvCorrection())
					.orElse(new RvCorrection(RvCorrection.UNDEFINED, 0))
						.getValue());
		result.setJulianDate(spectrum.getJulianDate());
		
		return result;
	}
}
