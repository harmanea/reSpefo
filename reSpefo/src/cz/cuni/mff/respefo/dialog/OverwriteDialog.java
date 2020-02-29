package cz.cuni.mff.respefo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import cz.cuni.mff.respefo.util.GridLayoutBuilder;

public class OverwriteDialog extends Dialog {
	public int choice = 0; // 0 - cancel, 1 - overwrite, 2 - append, 3 - to new file
	
	public OverwriteDialog(Shell parent) {
		super(parent, 0);
	}

	public int open() {
		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("File already exists");
		shell.setLayout(GridLayoutBuilder.gridLayout(1, false).verticalSpacing(25).build());
		
		final Label label = new Label(shell, SWT.LEFT);
		label.setText("File already exists. How do you want to save the results?");
		
		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(GridLayoutBuilder.gridLayout(4, true).margins(0).build());
		
		final Button overwriteButton = new Button(composite, SWT.PUSH | SWT.CENTER);
		overwriteButton.setText("Overwrite");
		overwriteButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		overwriteButton.addListener(SWT.Selection, event -> setChoiceAndDisposeShell(1, shell));
		
		final Button appendButton = new Button(composite, SWT.PUSH | SWT.CENTER);
		appendButton.setText("Append");
		appendButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		appendButton.addListener(SWT.Selection, event -> setChoiceAndDisposeShell(2, shell));
		
		final Button newFileButton = new Button(composite, SWT.PUSH | SWT.CENTER);
		newFileButton.setText("New File");
		newFileButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		newFileButton.addListener(SWT.Selection, event -> setChoiceAndDisposeShell(3, shell));
		
		final Button cancelButton = new Button(composite, SWT.PUSH | SWT.CENTER);
		cancelButton.setText("Cancel");
		cancelButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		cancelButton.addListener(SWT.Selection, event -> setChoiceAndDisposeShell(0, shell));
		
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return choice;
	}
	
	private void setChoiceAndDisposeShell(int choice, Shell shell) {
		this.choice = choice;
		shell.dispose();
	}
}
