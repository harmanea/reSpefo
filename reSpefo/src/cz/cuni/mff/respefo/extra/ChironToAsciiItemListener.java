package cz.cuni.mff.respefo.extra;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Paths;

import org.eclipse.swt.events.SelectionEvent;

import cz.cuni.mff.respefo.listeners.AbstractSelectionListener;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.Message;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;

public class ChironToAsciiItemListener extends AbstractSelectionListener {
	private static ChironToAsciiItemListener instance;
	
	private ChironToAsciiItemListener() {}
	
	public static ChironToAsciiItemListener getInstance() {
		if (instance == null) {
			instance = new ChironToAsciiItemListener();
		}
		
		return instance;
	}

	public void handle(SelectionEvent event) {
		String fileName = FileUtils.fileOpenDialog(FileType.SPECTRUM);
		
		if (fileName == null) {
			return;
		}
		
		FitsFactory.setAllowHeaderRepairs(true);
		try (Fits fits = new Fits(fileName)) {
			BasicHDU<?>[] HDUs = fits.read();
			
			BasicHDU<?> hdu = HDUs[0];
			float[][][] data = (float[][][]) hdu.getKernel();
			
			printHeaderToFile(fileName, hdu);
			printDataToFile(fileName, data);
			
		} catch (Exception exception) {
			Message.error("Couldn't convert file.", exception);
			return;
		} finally {
			FitsFactory.setAllowHeaderRepairs(false);
		}
		
		Message.info("File converted successfully.");
	}
	
	private void printHeaderToFile(String fileName, BasicHDU<?> hdu) throws IOException {
		String headerFile = fileName.substring(0, fileName.lastIndexOf('.')) + ".header";
		try (PrintStream ps = new PrintStream(headerFile)) {
			hdu.getHeader().dumpHeader(ps);
			
			if (ps.checkError()) {
				throw new IOException("PrintStream encountered an error.");
			}
		}
	}
	
	private void printDataToFile(String fileName, float[][][] data) throws IOException {
		String asciiFile = fileName.substring(0, fileName.lastIndexOf('.')) + ".txt";
		
		try (PrintWriter writer = new PrintWriter(asciiFile)) {
			writer.println(Paths.get(fileName).getFileName().toString());
			
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					for (int k = 0; k < data[i][j].length; k++) {
						if (k % 2 == 0) {
							writer.print(data[i][j][k] + " ");
						} else {
							writer.println(data[i][j][k]);
						}
					}
				}
			}
			
			if (writer.checkError()) {
				throw new IOException("PrintWriter encountered an error.");
			}
		}
	}
}
