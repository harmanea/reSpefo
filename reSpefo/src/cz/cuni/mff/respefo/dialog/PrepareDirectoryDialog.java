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

public class PrepareDirectoryDialog extends Dialog {
	private boolean status;

	private String lstFile, projectPrefix;
	private boolean applyCorrection;
	
	private Composite compOne, compTwo, compThree;
	private Label labelOne, labelTwo;
	private Text lstFileField, projectPrefixField;
	private Button buttonBrowse, checkboxApplyCorr, buttonOk, buttonCancel;

	public String getLstFile() {
		return lstFile;
	}

	public String getProjectPrefix() {
		return projectPrefix;
	}

	public boolean applyCorrection() {
		return applyCorrection;
	}
	
	public PrepareDirectoryDialog(Shell parent) {
		super(parent, 0);
		 
		status = false;
	}
	
	public boolean open() {
		Display display = getParent().getDisplay();
		Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Prepare directory");
		
        GridLayout layout = new GridLayout(1, false);
        layout.marginBottom = 15;
        layout.marginLeft = 15;
        layout.marginRight = 15;
        layout.marginTop = 15;
        layout.verticalSpacing = 10;
        shell.setLayout(layout);
		
		// Part one
		
		compOne = new Composite(shell, SWT.NONE);
        layout = new GridLayout(2, false);
        compOne.setLayout(layout);
        compOne.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        labelOne = new Label(compOne, SWT.LEFT);
        labelOne.setText("Select .lst file");
        labelOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        
        lstFileField = new Text(compOne, SWT.BORDER);
        lstFileField.setText("");
        lstFileField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        lstFileField.setEnabled(false);
  
        buttonBrowse = new Button(compOne, SWT.PUSH | SWT.CENTER);
        buttonBrowse.setText("   Browse...   ");
        buttonBrowse.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
        
        // Part two
        
        compTwo = new Composite(shell, SWT.NONE);
        layout = new GridLayout(2, false);
        compTwo.setLayout(layout);
        compTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        labelTwo = new Label(compTwo, SWT.LEFT);
        labelTwo.setText("Project prefix:");
        labelTwo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        
        projectPrefixField = new Text(compTwo, SWT.BORDER);
        projectPrefixField.setText("");
        projectPrefixField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Part three
        
        checkboxApplyCorr = new Button(shell, SWT.CHECK);
        checkboxApplyCorr.setText("Apply RV correction");
        checkboxApplyCorr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Part four
        
        compThree = new Composite(shell, SWT.NONE);
        layout = new GridLayout(2, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        compThree.setLayout(layout);
        compThree.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
        
        buttonOk = new Button(compThree, SWT.PUSH | SWT.CENTER);
        buttonOk.setText("Ok");
        buttonOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        buttonCancel = new Button(compThree, SWT.PUSH | SWT.CENTER);
        buttonCancel.setText("  Cancel  ");
        buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));   
        
        // listeners
        
        buttonBrowse.addListener(SWT.Selection, event -> browse());
        projectPrefixField.addListener(SWT.Modify, event -> verify());
		buttonOk.addListener(SWT.Selection, event -> setStatusAndCloseShell(true, shell));
		buttonCancel.addListener(SWT.Selection, event -> setStatusAndCloseShell(false, shell));
        
		verify();
		
		shell.pack();
		shell.open();
		shell.setSize(600, 260);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
	
	private void browse() {
		String s = FileUtils.fileOpenDialog(FileType.LST);
		
		if (s != null) {
			lstFileField.setText(s);
			verify();
		}
	}
	
	private void verify() {
		if (lstFileField.getText().length() > 0 && projectPrefixField.getText().length() > 0) {
			buttonOk.setEnabled(true);
		} else {
			buttonOk.setEnabled(false);
		}
	}
	
	private void setStatusAndCloseShell(boolean status, Shell shell) {
		this.status = status;
		
		lstFile = lstFileField.getText();
		projectPrefix = projectPrefixField.getText();
		applyCorrection = checkboxApplyCorr.getSelection();
		
		shell.close();
	}
}
