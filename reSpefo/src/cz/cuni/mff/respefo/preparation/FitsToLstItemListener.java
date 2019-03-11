package cz.cuni.mff.respefo.preparation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import cz.cuni.mff.respefo.FitsSpectrum;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.rvResult.LstFile;
import cz.cuni.mff.respefo.rvResult.LstFileRecord;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;

public class FitsToLstItemListener implements SelectionListener {
	private static final Logger LOGGER = Logger.getLogger(FitsToLstItemListener.class.toString());
	
	private static FitsToLstItemListener instance;
	private FitsToLstItemListener() {}
	
	public static FitsToLstItemListener getInstance() {
		if (instance == null) {
			instance = new FitsToLstItemListener();
		}
		
		return instance;
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		handle(event);
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		handle(event);
	}

	private void handle(SelectionEvent event) {
		FitsToLstDialog dialog = new FitsToLstDialog(ReSpefo.getShell());
		if(!dialog.open()) {
			return;
		}
		
		String directoryName = dialog.getDirectoryName();
		if (directoryName == null) {
			return;
		}
		boolean nestedDirectories = dialog.isNestedDirectories();
		String header = dialog.getHeader();
		
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
			
			file.getRecords().stream().forEach(System.out::println);
		} catch (IOException exception) {
			Message.error("Error reading files", exception);
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
	
	private LstFileRecord lstFileRecordFromSpectrum(FitsSpectrum spectrum) { // TODO this needs to be better
		LstFileRecord result = new LstFileRecord();
		
		result.setExp(spectrum.getExpTime());
		result.setDate(spectrum.getDate());
		result.setFileName(spectrum.getName());
		
		return result;
	}
}
