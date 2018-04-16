package cz.cuni.mff.respefo.Listeners;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpectrumPrinter;
import nom.tam.fits.FitsException;

public class FileExportItemListener implements SelectionListener {

	public FileExportItemListener() {}
	
	@Override
	public void widgetSelected(SelectionEvent event) {
		MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_WARNING | SWT.OK);
		
		Spectrum spectrum = ReSpefo.getSpectrum();
		
		if (spectrum == null) { 
			mb.setMessage("No file loaded, nothing to export.");
			mb.open();
			return;
		}
		
		FileDialog dialog = new FileDialog(ReSpefo.getShell(), SWT.SAVE);
		
		String[] filterNames = new String[] { "Spectrum Files", "All Files (*)" };
		String[] filterExtensions = new String[] { "*.txt;*.fits;*.fit;*.fts;*.rui;*.uui", "*" };
		//String platform = SWT.getPlatform();

		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFileName(spectrum.name());

		String s = dialog.open();
		String extension;
		if (s == null) {
			return;
		} else {
			int i = s.lastIndexOf('.');
			if (i < s.length()) {
				extension = s.substring(i + 1);
			} else {
				extension = "";
			}
		}
		
		switch (extension) {
		case "txt":
			PrintWriter writer;
			try {
				writer = new PrintWriter(s);
			} catch (FileNotFoundException e) {
				mb.setMessage("Couldn't find file.");
				mb.open();
				return;
			}
			try {
				SpectrumPrinter.exportToASCIIFIle(writer, spectrum);
			} catch (IOException e) {
				mb.setMessage("Error occured while printing to file.");
				mb.open();
			}
			break;
		case "fits":
		case "fit":
		case "fts":
			try {
				SpectrumPrinter.exportToFitsFile(s, spectrum);
			} catch (IOException | FitsException e) {
				mb.setMessage("Error occured while printing to file.");
				mb.open();
			}
			break;
		case "rui":
		case "uui":
			mb.setMessage("Old Spefo formats aren't supported yet.");
			mb.open();
			break;
		default:
			mb.setMessage("Not a supported file type.");
			mb.open();
			break;
		}
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}

}
