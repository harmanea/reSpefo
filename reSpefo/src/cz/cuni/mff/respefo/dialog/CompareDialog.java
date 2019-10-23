package cz.cuni.mff.respefo.dialog;

import static cz.cuni.mff.respefo.util.GridLayoutBuilder.gridLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;

public class CompareDialog extends Dialog {
	private String fileA;
	private String fileB;
	
	private boolean status = false;
	
	private Text fieldA, fieldB;
	private Button buttonOk;

	public CompareDialog(Shell parent) {
		super(parent);
	}
	
	public String getFileA() {
		return fileA;
	}
	
	public String getFileB() {
		return fileB;
	}

	public boolean open() {
		// Shell
		
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Compare");
		
		GridLayout layout = gridLayout()
				.margins(15)
				.verticalSpacing(15)
				.build();
		shell.setLayout(layout);
		
		// File A
		
		final Composite fileAComp = new Composite(shell, SWT.NONE);
		fileAComp.setLayout(new GridLayout(2, false));
		fileAComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Label labelA = new Label(fileAComp, SWT.LEFT);
		labelA.setText("File A:"); // TODO: change this to something better
		labelA.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		fieldA = new Text(fileAComp, SWT.BORDER);
		fieldA.setText("");
		fieldA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fieldA.addListener(SWT.Modify, event -> validate());

		final Button browseA = new Button(fileAComp, SWT.PUSH | SWT.CENTER);
		browseA.setText("   Browse...   ");
		browseA.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		browseA.addListener(SWT.Selection, event -> browseForFile(fieldA));
		
		
		// File B
		
		final Composite fileBComp = new Composite(shell, SWT.NONE);
		fileBComp.setLayout(new GridLayout(2, false));
		fileBComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Label labelB = new Label(fileBComp, SWT.LEFT);
		labelB.setText("File B:"); // TODO: change this to something better
		labelB.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		fieldB = new Text(fileBComp, SWT.BORDER);
		fieldB.setText("");
		fieldB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fieldB.addListener(SWT.Modify, event -> validate());

		final Button browseB = new Button(fileBComp, SWT.PUSH | SWT.CENTER);
		browseB.setText("   Browse...   ");
		browseB.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		browseB.addListener(SWT.Selection, event -> browseForFile(fieldB));
		
		
		// Buttons
		
		Composite buttonsComp = new Composite(shell, SWT.NONE);
		buttonsComp.setLayout(gridLayout(2, true).margins(0).build());
		buttonsComp.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));

		buttonOk = new Button(buttonsComp, SWT.PUSH | SWT.CENTER);
		buttonOk.setText("Ok");
		buttonOk.setEnabled(false);
		buttonOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonOk.addListener(SWT.Selection, event -> confirmAndCloseDialog(shell));

		final Button buttonCancel = new Button(buttonsComp, SWT.PUSH | SWT.CENTER);
		buttonCancel.setText("  Cancel  ");
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonCancel.addListener(SWT.Selection, event -> setStatusAndCloseShell(false, shell));
		
		
		// Main loop
		
		shell.pack();
		shell.open();
		shell.setSize(500, 280);
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
	
	private void validate() {
		if (!fieldA.getText().isEmpty() && !fieldB.getText().isEmpty()) {
			buttonOk.setEnabled(true);
		} else {
			buttonOk.setEnabled(false);
		}
	}
	
	private void browseForFile(Text textField) {
		String fileName = FileUtils.fileOpenDialog(FileType.SPECTRUM);
		if (fileName != null) {
			textField.setText(fileName);
		}
	}
	
	private void confirmAndCloseDialog(Shell shell) {
		fileA = fieldA.getText();
		fileB = fieldB.getText();
		
		setStatusAndCloseShell(true, shell);
	}
	
	private void setStatusAndCloseShell(boolean status, Shell shell) {
		this.status = status;
		shell.close();
	}
}
