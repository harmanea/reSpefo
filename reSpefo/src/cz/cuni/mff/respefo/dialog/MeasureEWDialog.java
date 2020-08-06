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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;

public class MeasureEWDialog extends Dialog {
	private Shell parent;
	private boolean status;
	
	private static String fileName;
	private static String[] items = {};
	
	private Text spectrumField;
	private List itemsList;
	private Button buttonOk;
	
	public MeasureEWDialog(Shell parent) {
		super(parent, 0);
		this.parent = parent;
		status = false;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String[] getItems() {
		return items;
	}
	
	public boolean open() {
		Display display = parent.getDisplay();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Measure EW");
		
        GridLayout layout = new GridLayout(1, false);
        layout.marginBottom = 15;
        layout.marginLeft = 15;
        layout.marginRight = 15;
        layout.marginTop = 15;
        layout.verticalSpacing = 10;
        shell.setLayout(layout);
		
		// Part one
		
		Composite compOne = new Composite(shell, SWT.NONE);
        layout = new GridLayout(2, false);
        compOne.setLayout(layout);
        compOne.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Label labelOne = new Label(compOne, SWT.LEFT);
        labelOne.setText("Select spectrum to measure");
        labelOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        
        spectrumField = new Text(compOne, SWT.BORDER);
        spectrumField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setSpectrumFieldText();
        spectrumField.addModifyListener(event -> validate());
  
        Button buttonBrowse = new Button(compOne, SWT.PUSH | SWT.CENTER);
        buttonBrowse.setText("   Browse...   ");
        buttonBrowse.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
        buttonBrowse.addListener(SWT.Selection, event -> browseForSpectrumFileAndValidate());
        
        // Part two
        
        Composite compTwo = new Composite(shell, SWT.NONE);
        layout = new GridLayout(2, false);
        compTwo.setLayout(layout);
        compTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Label labelTwo = new Label(compTwo, SWT.LEFT);
        labelTwo.setText("Select .stl file(s) with measurements");
        labelTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        
        itemsList = new List(compTwo, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        itemsList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        itemsList.setItems(items);
        
        Composite buttonCompOne = new Composite(compTwo, SWT.NONE);
        layout = new GridLayout(1, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonCompOne.setLayout(layout);
        buttonCompOne.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
        
        Button buttonAdd = new Button(buttonCompOne, SWT.PUSH | SWT.CENTER);
        buttonAdd.setText("Add");
        buttonAdd.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonAdd.addListener(SWT.Selection, event -> addStlFileToListAndValidate());
        
        Button buttonRemove = new Button(buttonCompOne, SWT.PUSH | SWT.CENTER);
        buttonRemove.setText("Remove");
        buttonRemove.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonRemove.addListener(SWT.Selection, event -> removeFromListAndValidate());

        
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
		
		validate();
		
		shell.pack();
		shell.open();
		shell.setSize(600, 400);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
	
	private void setSpectrumFieldText() {
		if (fileName == null) {
			spectrumField.setText("");
		} else {
			String newFileName = FileUtils.incrementFileName(fileName);
			if (newFileName == null) {
				spectrumField.setText("");
			} else {
				spectrumField.setText(newFileName);
			}
		}
	}
	
	private void validate() {
		if (spectrumField.getText().equals("") || itemsList.getItemCount() == 0) {
			buttonOk.setEnabled(false);
		} else {
			buttonOk.setEnabled(true);
		}
	}
	
	private void browseForSpectrumFileAndValidate() {
		String fileName = FileUtils.fileOpenDialog(FileType.SPECTRUM);
		
		if (fileName != null) {
			spectrumField.setText(fileName);
			spectrumField.setSelection(spectrumField.getText().length());
			
			validate();
		}
	}
	
	private void confirmAndCloseDialog(Shell shell) {
		items = itemsList.getItems();
		fileName = spectrumField.getText();
		
		setStatusAndCloseShell(true, shell);
	}
	
	private void setStatusAndCloseShell(boolean status, Shell shell) {
		this.status = status;
		shell.close();
	}
	
	private void addStlFileToListAndValidate() {
		String fileName = FileUtils.fileOpenDialog(FileType.STL, false);
		
		if (fileName != null) {
			itemsList.add(fileName);
			
			validate();
		}
	}
	
	private void removeFromListAndValidate() {
		if (itemsList.getSelectionIndex() != -1) {
			itemsList.remove(itemsList.getSelectionIndex());
		}
		
		validate();
	}
}
