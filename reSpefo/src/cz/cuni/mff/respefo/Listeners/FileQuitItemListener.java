package cz.cuni.mff.respefo.Listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;

import cz.cuni.mff.respefo.ReSpefo;

public class FileQuitItemListener implements SelectionListener {


	@Override
	public void widgetSelected(SelectionEvent event) {
		MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
		mb.setMessage("Are you sure you want to quit?");
		if (mb.open() == SWT.YES) {
			System.exit(0);
		}	
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		this.widgetSelected(event);
	}


}
