package cz.cuni.mff.respefo.preparation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.OldSpefoSpectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.listeners.AbstractSelectionListener;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;

public class UuiToFitsItemListener extends AbstractSelectionListener {
	private static UuiToFitsItemListener instance;

	private UuiToFitsItemListener() {
		LOGGER.log(Level.FINEST, "Creating a new UuiToFitsItemListener");
	}

	public static UuiToFitsItemListener getInstance() {
		if (instance == null) {
			instance = new UuiToFitsItemListener();
		}
		
		return instance;
	}

	
	
	@Override
	public void handle(SelectionEvent event) {
		// TODO: add a specialized dialog
		String directoryName = FileUtils.directoryDialog();
		boolean nestedDirectories = true;
		
		try (Stream<Path> paths = Files.walk(Paths.get(directoryName), nestedDirectories ? Integer.MAX_VALUE : 1)) {
			paths.parallel()
					.filter(this::isUuiFile)
					.map(this::spectrumFromPath)
					.filter(Objects::nonNull)
					.forEach(spectrum -> exportToFits(spectrum));
			
		} catch (IOException exception) {
			Message.error("Error reading files.", exception);
		}
	}

	private boolean isUuiFile(Path path) {
		return Files.isRegularFile(path) && (path.toString().endsWith(".uui"));
	}
	
	private OldSpefoSpectrum spectrumFromPath(Path path) {
		try {
			return new OldSpefoSpectrum(path.toString());
		} catch (SpefoException exception) {
			LOGGER.log(Level.WARNING, "File [" + path + "] couldn't be loaded and was skipped.", exception);
			return null;
		}
	}
	
	private void exportToFits(OldSpefoSpectrum spectrum) {
		String fileName = FileUtils.stripFileExtension(spectrum.getFileName()) + ".fits";
		
		if (!spectrum.exportToFits(fileName)) {
			LOGGER.log(Level.WARNING, "File [" + spectrum.getFileName() + "] couldn't be saved and was skipped.");
		}
	}
}
