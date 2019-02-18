package cz.cuni.mff.respefo.chiron;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Util;
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
	public void widgetDefaultSelected(SelectionEvent e) {
		handle(e);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		handle(e);
	}

	private void handle(SelectionEvent e) {
		String fileName = Util.openFileDialog(Util.SPECTRUM_LOAD);
		
		if (fileName == null) {
			return;
		}
		
		FitsFactory.setAllowHeaderRepairs(true);
		try (Fits f = new Fits(fileName)) {
			BasicHDU<?>[] HDUs = f.read();
			
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
		} catch (Exception ex) {
			MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
			mb.setMessage("Couldn't convert file.");
			mb.open();
			return;
		} finally {
			FitsFactory.setAllowHeaderRepairs(false);
		}
		
		MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
		mb.setMessage("File converted successfully.");
		mb.open();
	}
}
