package cz.cuni.mff.respefo.measureRV;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class MeasurementInputDialog extends Dialog {
	private String value;
	private String comment;
	private boolean corr;
	public boolean editable;

	public MeasurementInputDialog(Shell parent) {
		super(parent, 0);
		
		this.comment = "";
		editable = true;
	}
	
	public MeasurementInputDialog(Shell parent, String value, boolean editable) {
		super(parent, 0);
		
		this.value = value;
		this.comment = "";
		this.editable = editable;
	}
	
	public MeasurementInputDialog(Shell parent, String value, String comment, boolean editable) {
		super(parent, 0);
		
		this.value = value;
		this.comment = comment;
		this.editable = editable;
	}

	public String getComment() {
		return comment;
	}

	public String open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Confirm measurement");

		GridLayout layout = new GridLayout(2, false);
		shell.setLayout(layout);

		Label labelOne = new Label(shell, SWT.LEFT);
		labelOne.setText("Category:");
		labelOne.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		Text textOne = new Text(shell, SWT.SINGLE | SWT.BORDER);
		textOne.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		if (value != null) { 
			textOne.setText(value);
			value = null;
		} else {
			textOne.setText("");
		}
		
		if (!editable) {
			textOne.setEnabled(false);
		}

		Label labelTwo = new Label(shell, SWT.LEFT);
		labelTwo.setText("Comment:");
		labelTwo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		Text textTwo = new Text(shell, SWT.SINGLE | SWT.BORDER);
		textTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textTwo.setText(comment);

		Button buttonConfirm = new Button(shell, SWT.PUSH | SWT.CENTER);
		buttonConfirm.setText("Confirm");
		buttonConfirm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (textOne.getText().equals("")) {
			buttonConfirm.setEnabled(false);
		}

		Button buttonCancel = new Button(shell, SWT.PUSH | SWT.CENTER);
		buttonCancel.setText("Cancel");
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		shell.setDefaultButton(buttonConfirm);

		if (!corr) {
			textOne.addListener(SWT.Modify, event -> buttonConfirm.setEnabled(!textOne.getText().equals("")));
		}

		buttonConfirm.addListener(SWT.Selection, event -> setValueCommentAndDisposeShell(textOne, textTwo, shell));
		buttonCancel.addListener(SWT.Selection, event -> shell.dispose());

		shell.pack();
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return value;
	}
	
	private void setValueCommentAndDisposeShell(Text valueText, Text commentText, Shell shell) {
		value = valueText.getText();
		comment = commentText.getText();
		shell.dispose();
	}
}
