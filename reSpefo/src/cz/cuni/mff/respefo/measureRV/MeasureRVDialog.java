package cz.cuni.mff.respefo.measureRV;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.cuni.mff.respefo.Util;

public class MeasureRVDialog extends Dialog {
	private Shell parent;
	private boolean status;
	
	private String fileName;
	private String[] itemsOne, itemsTwo;
	
	private double rvStep; // negative means relative is selected
	
	private Composite compOne, compTwo, compThree, compFive;
	private Group group;
	private Composite buttonCompOne, buttonCompTwo, buttonCompThree, compGroup, compFixedVal;
	private Label labelOne, labelTwo, labelThree, labelFour, warningImage, warningText;
	private Text spectrumField, rvStepField;
	private Button buttonBrowse, buttonAddOne, buttonRemoveOne, buttonAddTwo, buttonRemoveTwo, buttonRadioOne, buttonRadioTwo, buttonOk, buttonCancel;
	private List listOne, listTwo;
	
	public MeasureRVDialog(Shell parent) {
		super(parent, 0);
		this.parent = parent;
		status = false;
	}
	
	public String getSpectrum() {
		return fileName;
	}
	
	public String[] getMeasurements() {
		return itemsOne;
	}
	
	public String[] getCorrections() {
		return itemsTwo;
	}
	
	public double getRvStep() {
		return rvStep;
	}
	
	public boolean open() {
		Display display = parent.getDisplay();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Measure radial velocity");
		
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
        labelOne.setText("Select spectrum to measure");
        labelOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        
        spectrumField = new Text(compOne, SWT.BORDER);
        spectrumField.setText("");
        spectrumField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
  
        buttonBrowse = new Button(compOne, SWT.PUSH | SWT.CENTER);
        buttonBrowse.setText("   Browse...   ");
        buttonBrowse.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));

        
        // Part two
        
        compTwo = new Composite(shell, SWT.NONE);
        layout = new GridLayout(2, false);
        compTwo.setLayout(layout);
        compTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        labelTwo = new Label(compTwo, SWT.LEFT);
        labelTwo.setText("Select .stl file(s) with measurements");
        labelTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        
        listOne = new List(compTwo, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        listOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        buttonCompOne = new Composite(compTwo, SWT.NONE);
        layout = new GridLayout(1, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonCompOne.setLayout(layout);
        buttonCompOne.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
        
        buttonAddOne = new Button(buttonCompOne, SWT.PUSH | SWT.CENTER);
        buttonAddOne.setText("Add");
        buttonAddOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        buttonRemoveOne = new Button(buttonCompOne, SWT.PUSH | SWT.CENTER);
        buttonRemoveOne.setText("Remove");
        buttonRemoveOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Part three
        
        compThree = new Composite(shell, SWT.NONE);
        layout = new GridLayout(2, false);
        compThree.setLayout(layout);
        compThree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        labelThree = new Label(compThree, SWT.LEFT);
        labelThree.setText("Select .stl file(s) with telluric lines");
        labelThree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        
        listTwo = new List(compThree, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        listTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        buttonCompTwo = new Composite(compThree, SWT.NONE);
        layout = new GridLayout(1, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonCompTwo.setLayout(layout);
        buttonCompTwo.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
        
        buttonAddTwo = new Button(buttonCompTwo, SWT.PUSH | SWT.CENTER);
        buttonAddTwo.setText("Add");
        buttonAddTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        buttonRemoveTwo = new Button(buttonCompTwo, SWT.PUSH | SWT.CENTER);
        buttonRemoveTwo.setText("Remove");
        buttonRemoveTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        
        // Part four
        
        group = new Group(shell, SWT.RADIO);
        group.setText("RV step");
        layout = new GridLayout(1, true);
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        compGroup = new Composite(group, SWT.NONE);
		layout = new GridLayout(1, true);
        layout.marginWidth = 10;
        layout.marginTop = 0;
        layout.marginBottom = 15;
		compGroup.setLayout(layout);
		compGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        compFixedVal = new Composite(compGroup, SWT.NONE);	
		layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginTop = 0;
        layout.marginBottom = 5;
		compFixedVal.setLayout(layout);
		compFixedVal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        buttonRadioOne = new Button(compFixedVal, SWT.RADIO);
        buttonRadioOne.setText("fixed value:");
		buttonRadioOne.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        
        rvStepField = new Text(compFixedVal, SWT.BORDER);
        rvStepField.setText("");
    	rvStepField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        labelFour = new Label(compFixedVal, SWT.RIGHT);
        labelFour.setText("km/s");
        labelFour.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        buttonRadioTwo = new Button(compGroup, SWT.RADIO);
        buttonRadioTwo.setText("relative to scale");
        buttonRadioTwo.setSelection(true);
        buttonRadioTwo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        
        // Part five
        
        compFive = new Composite(shell, SWT.NONE);
        layout = new GridLayout(3, false);
        compFive.setLayout(layout);
        compFive.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        warningImage = new Label(compFive, SWT.NONE);
        warningImage.setImage(shell.getDisplay().getSystemImage(SWT.ICON_WARNING));
        warningImage.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        
        warningText = new Label(compFive, SWT.NONE);
        warningText.setText("There is a problem with the inputs.");
        warningText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        buttonCompThree = new Composite(compFive, SWT.NONE);
        layout = new GridLayout(2, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonCompThree.setLayout(layout);
        buttonCompThree.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        
        buttonOk = new Button(buttonCompThree, SWT.PUSH | SWT.CENTER);
        buttonOk.setText("Ok");
        buttonOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        buttonCancel = new Button(buttonCompThree, SWT.PUSH | SWT.CENTER);
        buttonCancel.setText("  Cancel  ");
        buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));     
       
        
        // Listeners
        spectrumField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				validate();
			}
		});
        
		buttonBrowse.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				String s = Util.openFileDialog(Util.SPECTRUM_LOAD);
				
				if (s != null) {
					spectrumField.setText(s);
					spectrumField.setSelection(spectrumField.getText().length());
					
					validate();
				}
			}

		});
		
		buttonAddOne.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				String s = Util.openFileDialog(Util.STL_LOAD);
				
				if (s != null) {
					listOne.add(s);
					
					validate();
				}
			}

		});
		
		buttonRemoveOne.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				if (listOne.getSelectionIndex() != -1) {
					listOne.remove(listOne.getSelectionIndex());
					
					validate();
				}
			}

		});
		
		buttonAddTwo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				String s = Util.openFileDialog(Util.STL_LOAD);
				
				if (s != null) {
					listTwo.add(s);
				}
			}

		});
		
		buttonRemoveTwo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				if (listTwo.getSelectionIndex() != -1) {
					listTwo.remove(listTwo.getSelectionIndex());
				}
			}

		});
		
		buttonRadioOne.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonRadioTwo.setSelection(false);
				
				validate();
			}
		});
		
		buttonRadioTwo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonRadioOne.setSelection(false);
				
				validate();
			}
		});
		
		rvStepField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				
				validate();
			}
		});
		
		buttonOk.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				status = true;
				itemsOne = listOne.getItems();
				itemsTwo = listTwo.getItems();
				fileName = spectrumField.getText();
				if (buttonRadioTwo.getSelection()) {
					rvStep = -1;
				} else {
					rvStep = Double.parseDouble(rvStepField.getText());
				}
				shell.dispose();
			}
		});
		
		buttonCancel.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				status = false;
				shell.dispose();
			}
		});
		
		// Pack and open
		validate();
		
		shell.pack();
		shell.open();
		shell.setSize(600, 700);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
	
	private void validate() {
		String message = null;
		
		if (!spectrumField.getText().equals("")) {
			File file = new File(spectrumField.getText());
			if (!file.exists()) {
				message = "Spectrum file doesn't exist.";
				
			} else if (listOne.getItemCount() == 0) {
				message = "Add at least one file with measurements.";
				
			} else if (buttonRadioOne.getSelection()) {
				try {
					double val = Double.parseDouble(rvStepField.getText());
					
					if (val < 0) {
						message = "RV step must be a positive number.";
					}
				} catch (NumberFormatException e) {
					message = "RV step must be a valid number";
					
				}
			}
		} else {
			message = "Select a spectrum file.";
		}
		
		if (message == null) {
			buttonOk.setEnabled(true);
			
			warningImage.setVisible(false);
			warningText.setVisible(false);
		} else {
			buttonOk.setEnabled(false);
			
			warningImage.setVisible(true);
			
			warningText.setText(message);
			warningText.setVisible(true);
		}
	}
}
