package cz.cuni.mff.respefo.dialog;

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

import cz.cuni.mff.respefo.util.FileUtils;

public class FitsToLstDialog extends Dialog {
	private Shell parent;
	private boolean status;
	private boolean nestedDirectories = false;
	private String directoryName, header;

	public boolean isNestedDirectories() {
		return nestedDirectories;
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public String getHeader() {
		return header;
	}

	private Label labelOne, labelTwo;
	private Text directoryField, headerField;
	private Button buttonBrowse, buttonOk, buttonCancel, checkboxNestedDirectories;

	public FitsToLstDialog(Shell parent) {
		super(parent);

		this.parent = parent;
		status = false;
	}

	public boolean open() {
		Display display = parent.getDisplay();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Fits to Lst");

		GridLayout layout = new GridLayout(1, false);
		layout.marginBottom = 15;
		layout.marginLeft = 15;
		layout.marginRight = 15;
		layout.marginTop = 15;
		layout.verticalSpacing = 10;
		shell.setLayout(layout);

		// Part one

		Composite compDirectory = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, false);
		compDirectory.setLayout(layout);
		compDirectory.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		labelOne = new Label(compDirectory, SWT.LEFT);
		labelOne.setText("Select directory to search");
		labelOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		directoryField = new Text(compDirectory, SWT.BORDER);
		directoryField.setText("");
		directoryField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		buttonBrowse = new Button(compDirectory, SWT.PUSH | SWT.CENTER);
		buttonBrowse.setText("   Browse...   ");
		buttonBrowse.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		buttonBrowse.addListener(SWT.Selection, event -> browseForDirectory());

		// Part two


		checkboxNestedDirectories = new Button(shell, SWT.CHECK);
		checkboxNestedDirectories.setText("Search nested directories");
		checkboxNestedDirectories.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		checkboxNestedDirectories.addListener(SWT.Selection, event -> {
			nestedDirectories = checkboxNestedDirectories.getSelection();
		});

		// Part Three

		labelTwo = new Label(shell, SWT.LEFT);
		labelTwo.setText("Fill in the file header");
		labelTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		headerField = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		headerField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		headerField.addListener(SWT.Modify, event -> verifyAndSetHeader());

		// Part four

		Composite compButtons = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		compButtons.setLayout(layout);
		compButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));

		buttonOk = new Button(compButtons, SWT.PUSH | SWT.CENTER);
		buttonOk.setText("Ok");
		buttonOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonOk.addListener(SWT.Selection, event -> setStatusAndCloseShell(true, shell));

		buttonCancel = new Button(compButtons, SWT.PUSH | SWT.CENTER);
		buttonCancel.setText("  Cancel  ");
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonCancel.addListener(SWT.Selection, event -> setStatusAndCloseShell(false, shell));

		shell.pack();
		shell.open();
		shell.setSize(600, 500);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return status;
	}

	private void browseForDirectory() {
		String directoryName = FileUtils.directoryDialog();

		if (directoryName != null) {
			directoryField.setText(directoryName);
			directoryField.setSelection(directoryField.getText().length());
			this.directoryName = directoryName;
		}
	}
	
	private void verifyAndSetHeader() {
		if (headerField.getLineCount() > 4) {
			headerField.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			headerField.setToolTipText("Header can only have up to four lines.");
		} else {
			headerField.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
			headerField.setToolTipText(null);
			
			header = headerField.getText();
		}
	}

	private void setStatusAndCloseShell(boolean status, Shell shell) {
		this.status = status;
		shell.close();
	}
}
