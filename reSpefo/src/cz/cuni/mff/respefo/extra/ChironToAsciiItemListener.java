package cz.cuni.mff.respefo.extra;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.util.Util;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;

public class ChironToAsciiItemListener implements SelectionListener {
	private static ChironToAsciiItemListener instance;
	
	private ChironToAsciiItemListener() {}
	
	public static ChironToAsciiItemListener getInstance() {
		if (instance == null) {
			instance = new ChironToAsciiItemListener();
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
		String fileName = Util.openFileDialog(Util.SPECTRUM_LOAD);
		
		if (fileName == null) {
			return;
		}
		
		FitsFactory.setAllowHeaderRepairs(true);
		try (Fits fits = new Fits(fileName)) {
			BasicHDU<?>[] HDUs = fits.read();
			
			BasicHDU<?> hdu = HDUs[0];
			float[][][] data = (float[][][]) hdu.getKernel();
			
			String headerFile = fileName.substring(0, fileName.lastIndexOf('.')) + ".header";
			try (PrintStream ps = new PrintStream(headerFile)) {
				hdu.getHeader().dumpHeader(ps);
				
				if (ps.checkError()) {
					throw new IOException();
				}
			}
			
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
			}
		} catch (Exception exception) {
			MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			messageBox.setMessage("Couldn't convert file.");
			messageBox.open();
			return;
		} finally {
			FitsFactory.setAllowHeaderRepairs(false);
		}
		
		MessageBox messageBox = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
		messageBox.setMessage("File converted successfully.");
		messageBox.open();
	}
}
