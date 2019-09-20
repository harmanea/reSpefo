package cz.cuni.mff.respefo.dialog;

import static cz.cuni.mff.respefo.util.GridLayoutBuilder.gridLayout;

import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;

public class AddToLstDialog extends Dialog {
	private boolean status;
	private boolean includeFileName;
	private String lstFileName;
	private String[] fileNames;
	private String filePrefix = "";
	
	private Label labelOne, labelTwo, labelThree;
	private Text lstField, filePrefixField;
	private Button buttonBrowse, buttonAdd, buttonRemove, buttonCheck, buttonOk, buttonCancel;
	private List listFiles;

	public AddToLstDialog(Shell parent) {
		super(parent);
		
		status = false;
	}
	
	public boolean includeFileName() {
		return includeFileName;
	}
	
	public String getLstFileName() {
		return lstFileName;
	}
	
	public String[] getFileNames() {
		return fileNames;
	}
	
	public String getFilePrefix() {
		return filePrefix;
	}

	public boolean open() {
		final Display display = getParent().getDisplay();
		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Add to Lst");
		
		GridLayout layout = gridLayout()
				.margins(15)
				.verticalSpacing(10)
				.build();
		shell.setLayout(layout);
		
		// Part one
		
		Composite compLst = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, false);
		compLst.setLayout(layout);
		compLst.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		labelOne = new Label(compLst, SWT.LEFT);
		labelOne.setText("Select Lst file to append to");
		labelOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		lstField = new Text(compLst, SWT.BORDER);
		lstField.setText("");
		lstField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		lstField.addListener(SWT.Modify, event -> validate());

		buttonBrowse = new Button(compLst, SWT.PUSH | SWT.CENTER);
		buttonBrowse.setText("   Browse...   ");
		buttonBrowse.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		buttonBrowse.addListener(SWT.Selection, event -> browseForLstFile());

		// Part two
		
		Composite compFiles = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, false);
		compFiles.setLayout(layout);
		compFiles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		labelTwo = new Label(compFiles, SWT.LEFT);
		labelTwo.setText("Select files to append");
		labelTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		listFiles = new List(compFiles, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		listFiles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite compButtons = new Composite(compFiles, SWT.NONE);
		layout = gridLayout()
				.margins(0)
				.build();
		compButtons.setLayout(layout);
		compButtons.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
		
		buttonAdd = new Button(compButtons, SWT.PUSH | SWT.CENTER);
		buttonAdd.setText("Add");
		buttonAdd.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonAdd.addListener(SWT.Selection, event -> addFilesAndValidate());
		
		buttonRemove = new Button(compButtons, SWT.PUSH | SWT.CENTER);
		buttonRemove.setText("Remove");
		buttonRemove.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonRemove.addListener(SWT.Selection, event -> removeFileAndValidate());
		
		// Part three
		
		Composite compFilePrefix = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, false);
		compFilePrefix.setLayout(layout);
		compFilePrefix.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		labelThree = new Label(compFilePrefix, SWT.LEFT);
		labelThree.setText("File prefix:");
		labelThree.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
		
		filePrefixField = new Text(compFilePrefix, SWT.BORDER);
		filePrefixField.setText("");
		filePrefixField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Part four
		
		buttonCheck = new Button(shell, SWT.CHECK);
		buttonCheck.setText("Include file name");
		buttonCheck.setSelection(true);
		buttonCheck.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		// Part five
		
		Composite compBottomButtons = new Composite(shell, SWT.NONE);
		layout = gridLayout(2, true)
				.margins(0)
				.build();
		compBottomButtons.setLayout(layout);
		compBottomButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));

		buttonOk = new Button(compBottomButtons, SWT.PUSH | SWT.CENTER);
		buttonOk.setText("Ok");
		buttonOk.setEnabled(false);
		buttonOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonOk.addListener(SWT.Selection, event -> confirmAndCloseDialog(shell));

		buttonCancel = new Button(compBottomButtons, SWT.PUSH | SWT.CENTER);
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
	
	private void browseForLstFile() {
		String lstFileName = FileUtils.fileOpenDialog(FileType.LST);

		if (lstFileName != null) {
			lstField.setText(lstFileName);
			lstField.setSelection(lstFileName.length());
			this.lstFileName = lstFileName;
			
			if (filePrefixField.getText().isEmpty()) {
				String filePrefix = FileUtils.stripFileExtension(Paths.get(lstFileName).getFileName().toString());
				filePrefixField.setText(filePrefix);
			}
			
			validate();
		}
	}
	
	private void addFilesAndValidate() {
		FileUtils.multipleFilesDialog(FileType.SPECTRUM)
		.forEach(fileName -> listFiles.add(fileName));
		
		validate();
	}
	
	private void removeFileAndValidate() {
		if (listFiles.getSelectionIndex() != -1) {
			listFiles.remove(listFiles.getSelectionIndex());
		}
		
		validate();
	}
	
	private void validate() {
		if (!lstField.getText().isEmpty() && listFiles.getItems().length > 0) {
			buttonOk.setEnabled(true);
		} else {
			buttonOk.setEnabled(false);
		}
	}
	
	private void confirmAndCloseDialog(Shell shell) {
		includeFileName = buttonCheck.getSelection();
		lstFileName = lstField.getText();
		fileNames = listFiles.getItems();
		filePrefix = filePrefixField.getText();
		
		setStatusAndCloseShell(true, shell);
	}
	
	private void setStatusAndCloseShell(boolean status, Shell shell) {
		this.status = status;
		shell.close();
	}
}
