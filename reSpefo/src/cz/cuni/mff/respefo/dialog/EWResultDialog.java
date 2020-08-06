package cz.cuni.mff.respefo.dialog;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import cz.cuni.mff.respefo.component.LstFile;
import cz.cuni.mff.respefo.component.LstFileRecord;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.SpefoException;

public class EWResultDialog extends Dialog {
	private boolean status;
	
	private LstFile lstFile;
	private String[] eqwFileNames;
	
	private Button buttonBrowse, buttonSelect, buttonRemove, buttonFill, buttonOk, buttonCancel;
	private Text lstFileField;
	
	private Table table;
	private TableColumn indexColumn, dateColumn, fileNameColumn;
	
	public EWResultDialog(Shell parent) {
		super(parent, 0);
		
		status = false;
	}
	
	public boolean open() {
		Display display = getParent().getDisplay();
		Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Print RV Result");
		
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
		labelOne.setText("Select .lst file");
		labelOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		lstFileField = new Text(compOne, SWT.BORDER);
		lstFileField.setText("");
		lstFileField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		lstFileField.setEnabled(false);

		buttonBrowse = new Button(compOne, SWT.PUSH | SWT.CENTER);
		buttonBrowse.setText("   Browse...   ");
		buttonBrowse.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));

		// Part Two

		Composite compTwo = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, false);
		compTwo.setLayout(layout);
		compTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		table = new Table(compTwo, SWT.VIRTUAL | SWT.BORDER);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);

		indexColumn = new TableColumn(table, SWT.CENTER);
		indexColumn.setText("Index");

		dateColumn = new TableColumn(table, SWT.CENTER);
		dateColumn.setText("Date");

		fileNameColumn = new TableColumn(table, SWT.CENTER);
		fileNameColumn.setText("File name");

		table.setEnabled(false);

		Composite compButtons = new Composite(compTwo, SWT.NONE);
		layout = new GridLayout(1, true);
		compButtons.setLayout(layout);
		compButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		buttonSelect = new Button(compButtons, SWT.PUSH | SWT.CENTER);
		buttonSelect.setText("Select file");
		buttonSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		buttonSelect.setEnabled(false);

		buttonRemove = new Button(compButtons, SWT.PUSH | SWT.CENTER);
		buttonRemove.setText("Remove file");
		buttonRemove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		buttonRemove.setEnabled(false);

		buttonFill = new Button(compButtons, SWT.PUSH | SWT.CENTER);
		buttonFill.setText("Fill below");
		buttonFill.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		buttonFill.setEnabled(false);

		// Part Three

		Composite compThree = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		compThree.setLayout(layout);
		compThree.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		buttonOk = new Button(compThree, SWT.PUSH | SWT.CENTER);
		buttonOk.setText("Ok");
		buttonOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonOk.setEnabled(false);

		buttonCancel = new Button(compThree, SWT.PUSH | SWT.CENTER);
		buttonCancel.setText("  Cancel  ");
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// listeners

		buttonBrowse.addListener(SWT.Selection, event -> browse());
		table.addListener(SWT.Selection, event -> verifySelection());
		table.addListener(SWT.DefaultSelection, event -> selectEqwFile());
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeEqwFile();
				}
			};
		});
		buttonSelect.addListener(SWT.Selection, event -> selectEqwFile());
		buttonRemove.addListener(SWT.Selection, event -> removeEqwFile());
		buttonFill.addListener(SWT.Selection, event -> fillFilenamesBelow());

		buttonOk.addListener(SWT.Selection, event -> setStatusAndCloseShell(true, shell));
		buttonCancel.addListener(SWT.Selection, event -> setStatusAndCloseShell(false, shell));

		shell.pack();
		shell.open();
		shell.setSize(600, 700);
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
			
			table.removeAll();
			
			try {
				lstFile = new LstFile(s);
				String filePrefix = FileUtils.stripFileExtension(Paths.get(s).getFileName().toString());
				
				List<String> eqwFileNamesList = new ArrayList<>();
				for (LstFileRecord r : lstFile.getRecords()) {							
					TableItem item = new TableItem(table, SWT.NONE);
					String fileName = filePrefix + String.format("%05d", r.getIndex()) + ".eqw";
					String fileNameWithParent = FileUtils.getFilterPath() + File.separatorChar + fileName;
					
					item.setText(new String[] {Integer.toString(r.getIndex()), r.getFormattedDate(), fileName});
					if (!Files.exists(Paths.get(fileNameWithParent))) {
						item.setForeground(2, table.getDisplay().getSystemColor(SWT.COLOR_RED));
						
						eqwFileNamesList.add(null);
					} else {
						eqwFileNamesList.add(fileNameWithParent);
					}							
				}
				eqwFileNames = eqwFileNamesList.stream().toArray(String[]::new);
				
				indexColumn.pack();
				dateColumn.pack();
				fileNameColumn.pack();
				
				table.setEnabled(true);
				
				lstFileField.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));

			} catch (SpefoException e1) {
				lstFileField.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_RED));
			}
		}
		
		verifyFields();
	}
	
	private void selectEqwFile() {
		int index = table.getSelectionIndex();
		if (index < 0) {
			return;
		}
		
		String fileName = FileUtils.fileOpenDialog(FileType.EQW);
		if (fileName == null) {
			return;
		}
		
		eqwFileNames[index] = fileName;
		
		table.getItem(index).setText(2, Paths.get(fileName).getFileName().toString());
		table.getItem(index).setForeground(2, table.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		
		verifySelection();
		verifyFields();
	}
	
	private void removeEqwFile() {
		int index = table.getSelectionIndex();
		if (index < 0) {
			return;
		}
		
		eqwFileNames[index] = null;
		
		table.getItem(index).setText(2, "-");
		table.getItem(index).setForeground(2, table.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		
		verifySelection();
		verifyFields();
	}
	
	private void fillFilenamesBelow() {
		if (table.isEnabled()) {
			int index = table.getSelectionIndex();
			String fileName = eqwFileNames[index];
			
			if (fileName != null) {
				while (index < eqwFileNames.length - 1) {
					String newFileName = FileUtils.incrementFileName(fileName);
					if (newFileName == null) {
						break;
					}
					TableItem tableItem = table.getItem(index + 1);
					tableItem.setText(2, Paths.get(newFileName).getFileName().toString());
					
					if (Files.exists(Paths.get(newFileName))) {
						eqwFileNames[index + 1] = newFileName;
						
						tableItem.setForeground(2, table.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
					} else {
						eqwFileNames[index + 1] = null;
						
						tableItem.setForeground(2, table.getDisplay().getSystemColor(SWT.COLOR_RED));
					}
					
					fileName = newFileName;
					index++;
				}
			}
		}
		
		verifySelection();
		verifyFields();
	}
	
	private void verifyFields() {
		if (eqwFileNames != null &&
				Arrays.stream(eqwFileNames).anyMatch(s -> s != null) &&
				lstFile != null &&
				lstFile.recordsCount() == eqwFileNames.length) {
			buttonOk.setEnabled(true);
		} else {
			buttonOk.setEnabled(false);
		}
	}
	
	private void verifySelection() {
		int index = table.getSelectionIndex();
		if (index < 0) {
			buttonSelect.setEnabled(false);
			buttonRemove.setEnabled(false);
			buttonFill.setEnabled(false);
		} else {
			buttonSelect.setEnabled(true);
			if (eqwFileNames[index] != null) {
				buttonRemove.setEnabled(true);
				buttonFill.setEnabled(true);
			}
		}
	}
	
	private void setStatusAndCloseShell(boolean status, Shell shell) {
		this.status = status;
		shell.close();
	}

	public LstFile getLstFile() {
		return lstFile;
	}

	public String[] getEqwFileNames() {
		return eqwFileNames;
	}
}
