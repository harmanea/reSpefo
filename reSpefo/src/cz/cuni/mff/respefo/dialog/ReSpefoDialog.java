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

import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;

public abstract class ReSpefoDialog extends Dialog {
	protected boolean status = false;
	protected Shell shell;
	
	public ReSpefoDialog(Shell parent) {
		super(parent);
	}

	public ReSpefoDialog(Shell parent, int style) {
		super(parent, style);
	}
	
	public abstract boolean open();
	
	/**
	 * This method gets called when a change happens to a component added using this class. By default it has no implementation.
	 */
	protected void validate() {}
	
	protected void openDialog() {
		shell.pack();
		shell.open();
		
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	protected void newShell(String text) {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM);
		if (text != null) {
			shell.setText(text);
		}
	}
	
	protected void newShell() {
		newShell(null);
	}
	
	protected void setStatusAndCloseShell(boolean status) {
		this.status = status;
		shell.close();
	}
	
	protected void setShellGridLayout() {
		setShellGridLayout(1, false);
	}
	
	protected void setShellGridLayout(int numColumns, boolean makeColumnsEqualWidth) {
		GridLayout layout = new GridLayout(numColumns, makeColumnsEqualWidth);
        layout.marginHeight = 15;
        layout.marginWidth = 15;
        layout.verticalSpacing = 10;
        shell.setLayout(layout);
	}
	
	protected Text addFileField(FileType fileType) {
		return addFileField(fileType, null);
	}
	
	protected Text addFileField(FileType fileType, String labelText) {
		Composite composite = new Composite(shell, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        if (labelText != null) {
            Label label = new Label(composite, SWT.LEFT);
            label.setText(labelText);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        }
        
        Text fileField = new Text(composite, SWT.BORDER);
        fileField.setText("");
        fileField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
  
        Button buttonBrowse = new Button(composite, SWT.PUSH | SWT.CENTER);
        buttonBrowse.setText("   Browse...   ");
        buttonBrowse.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
        
        buttonBrowse.addListener(SWT.Selection, event -> browseForSpectrumFileAndValidate(fileField, fileType));
        
		return fileField;
	}
	
	private void browseForSpectrumFileAndValidate(Text field, FileType fileType) {
		String fileName = FileUtils.fileOpenDialog(fileType);
		
		if (fileName != null) {
			field.setText(fileName);
			field.setSelection(field.getText().length());
			
			validate();
		}
	}
}
