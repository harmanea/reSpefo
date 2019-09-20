package cz.cuni.mff.respefo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SelectValuesDialog extends Dialog {
	private boolean status;
	private Text textOne, textTwo;
	private Button buttonConfirm;
	private double xShift, yShift;
	
	public SelectValuesDialog(Shell parent) {
		super(parent);
		
		status = false;
	}
	
	public double getXShift() {
		return xShift;
	}
	
	public double getYShift() {
		return yShift;
	}
	
	public boolean open(String xShift, String yShift) {
		Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Select values");
		shell.setLayout(new GridLayout(2, false));
		
		Label labelOne = new Label(shell, SWT.LEFT);
		labelOne.setText("X shift:");
		labelOne.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		textOne = new Text(shell, SWT.SINGLE | SWT.BORDER);
		textOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textOne.setText(xShift);
		textOne.addListener(SWT.Modify, e -> verify());
		
		Label labelTwo = new Label(shell, SWT.LEFT);
		labelTwo.setText("Y shift:");
		labelTwo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		textTwo = new Text(shell, SWT.SINGLE | SWT.BORDER);
		textTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textTwo.setText(yShift);
		textTwo.addListener(SWT.Modify, e -> verify());
		
		buttonConfirm = new Button(shell, SWT.PUSH | SWT.CENTER);
		buttonConfirm.setText("Confirm");
		buttonConfirm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonConfirm.addListener(SWT.Selection, e -> setStatusAndCloseShell(shell, true));

		Button buttonCancel = new Button(shell, SWT.PUSH | SWT.CENTER);
		buttonCancel.setText("Cancel");
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonCancel.addListener(SWT.Selection, e -> setStatusAndCloseShell(shell, false));
		
		shell.setDefaultButton(buttonConfirm);
		verify();
		
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
	
	private void verify() {
		try {
			xShift = Double.parseDouble(textOne.getText());
			yShift = Double.parseDouble(textTwo.getText());
			
			buttonConfirm.setEnabled(true);
		} catch (NumberFormatException e) {
			buttonConfirm.setEnabled(false);
		}
	}
	
	private void setStatusAndCloseShell(Shell shell, boolean status) {
		this.status = status;
		
		shell.close();
	}
}
