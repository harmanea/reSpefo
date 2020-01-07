package cz.cuni.mff.respefo.dialog;

import static cz.cuni.mff.respefo.util.FileType.SPECTRUM;
import static cz.cuni.mff.respefo.util.GridLayoutBuilder.gridLayout;

import java.util.Arrays;

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

import cz.cuni.mff.respefo.util.FileUtils;

public class ConvertToDialog extends Dialog {
	private boolean status;
	private String[] fileNames;
	private String fileExtension;
	
	private List filesList;
	private Text fileExtensionField;

	public ConvertToDialog(Shell parent) {
		super(parent);
		
		status = false;
	}
	
	public String[] getFileNames() {
		return fileNames;
	}
	
	public String getFileExtension() {
		return fileExtension;
	}
	
	public boolean open(String defaultFileExtension) {
		final Display display = getParent().getDisplay();
		final Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Convert");
		
		GridLayout layout = gridLayout()
				.margins(15)
				.verticalSpacing(10)
				.build();
		shell.setLayout(layout);
		
		// Part one
		
		final Composite filesComposite = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, false);
		filesComposite.setLayout(layout);
		filesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Label labelOne = new Label(filesComposite, SWT.LEFT);
		labelOne.setText("Select files to convert");
		labelOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		filesList = new List(filesComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		filesList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Composite addRemoveButtonsComposite = new Composite(filesComposite, SWT.NONE);
		layout = gridLayout()
				.margins(0)
				.build();
		addRemoveButtonsComposite.setLayout(layout);
		addRemoveButtonsComposite.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
		
		final Button filesButton = new Button(addRemoveButtonsComposite, SWT.PUSH | SWT.CENTER);
		filesButton.setText("Add");
		filesButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesButton.addListener(SWT.Selection, event -> addFiles());
		
		final Button removeButton = new Button(addRemoveButtonsComposite, SWT.PUSH | SWT.CENTER);
		removeButton.setText("Remove");
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		removeButton.addListener(SWT.Selection, event -> removeSelected());
		
		// Part two
		
        final Composite extensionComposite = new Composite(shell, SWT.NONE);
        layout = new GridLayout(2, false);
        extensionComposite.setLayout(layout);
        extensionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        final Label labelTwo = new Label(extensionComposite, SWT.LEFT);
        labelTwo.setText("File extension:");
        labelTwo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
        
        fileExtensionField = new Text(extensionComposite, SWT.BORDER);
        fileExtensionField.setText(defaultFileExtension);
        fileExtensionField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		
		// Part three
		
		final Composite okCancelButtonsComposite = new Composite(shell, SWT.NONE);
		layout = gridLayout(2, true)
				.margins(0)
				.build();
		okCancelButtonsComposite.setLayout(layout);
		okCancelButtonsComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));

		final Button okButton = new Button(okCancelButtonsComposite, SWT.PUSH | SWT.CENTER);
		okButton.setText("Ok");
		okButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		okButton.addListener(SWT.Selection, event -> confirmAndCloseDialog(shell));

		final Button cancelButton = new Button(okCancelButtonsComposite, SWT.PUSH | SWT.CENTER);
		cancelButton.setText("  Cancel  ");
		cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cancelButton.addListener(SWT.Selection, event -> setStatusAndCloseShell(false, shell));
		
		// Pack and open
		
		shell.pack();
		shell.open();
		shell.setSize(600, 500);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
	
	private void addFiles() {
		String[] listItems = filesList.getItems();
		
		for (String fileName : FileUtils.multipleFilesDialog(SPECTRUM)) {
			if (!Arrays.stream(listItems).anyMatch(fileName::equals)) {
				filesList.add(fileName);
			}
		}
	}
 	
	private void removeSelected() {
		filesList.remove(filesList.getSelectionIndices());
	}
	
	private void confirmAndCloseDialog(Shell shell) {
		fileNames = filesList.getItems();
		fileExtension = fileExtensionField.getText();
		
		setStatusAndCloseShell(true, shell);
	}
	
	private void setStatusAndCloseShell(boolean status, Shell shell) {
		this.status = status;
		shell.close();
	}
}
