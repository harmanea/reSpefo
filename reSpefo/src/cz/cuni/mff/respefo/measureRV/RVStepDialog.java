package cz.cuni.mff.respefo.measureRV;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class RVStepDialog extends Dialog {
	private boolean status;
	private double rvStep;

	public RVStepDialog(Shell parent) {
		super(parent, 0);

		status = false;
		rvStep = -1;
	}
	
	public RVStepDialog(Shell parent, double rvStep) {
		super(parent, 0);
		
		status = false;
		this.rvStep = rvStep;
	}
	
	public double getRVStep() {
		return rvStep;
	}
	
	boolean open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Choose RV step");
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		shell.setLayout(layout);
        
        Composite compFixedVal = new Composite(shell, SWT.NONE);	
		layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginTop = 0;
        layout.marginBottom = 5;
		compFixedVal.setLayout(layout);
		compFixedVal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Button buttonRadioOne = new Button(compFixedVal, SWT.RADIO);
        buttonRadioOne.setText("fixed value:");
		buttonRadioOne.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        
        Text rvStepField = new Text(compFixedVal, SWT.BORDER);
    	rvStepField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label label = new Label(compFixedVal, SWT.RIGHT);
        label.setText("km/s");
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        Button buttonRadioTwo = new Button(shell, SWT.RADIO);
        buttonRadioTwo.setText("relative to scale");
        buttonRadioTwo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		
        if (rvStep < 0) {
        	rvStepField.setText("0");
        	buttonRadioTwo.setSelection(true);
        } else {
        	rvStepField.setText(Double.toString(rvStep));
        	buttonRadioOne.setSelection(true);
        }
        
        Composite compButtons = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginTop = 10;
		compButtons.setLayout(layout);
		compButtons.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
		Button buttonConfirm = new Button(compButtons, SWT.PUSH | SWT.CENTER);
		buttonConfirm.setText("Confirm");
		buttonConfirm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Button buttonCancel = new Button(compButtons, SWT.PUSH | SWT.CENTER);
		buttonCancel.setText("Cancel");
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		shell.setDefaultButton(buttonConfirm);
        
		buttonRadioOne.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonRadioTwo.setSelection(false);
				
				try {
					double val = Double.parseDouble(rvStepField.getText());
					
					if (val < 0) {
						buttonConfirm.setEnabled(false);
					}
				} catch (NumberFormatException exc) {
					buttonConfirm.setEnabled(false);
				}
			}
		});
		
		buttonRadioTwo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonRadioOne.setSelection(false);
				
				buttonConfirm.setEnabled(true);
			}
		});
		
		rvStepField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				try {
					double val = Double.parseDouble(rvStepField.getText());
					
					if (val < 0) {
						buttonConfirm.setEnabled(false);
					} else {
						buttonConfirm.setEnabled(true);
					}
				} catch (NumberFormatException exc) {
					buttonConfirm.setEnabled(false);
				}
			}
		});
		
		buttonConfirm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (buttonRadioOne.getSelection()) {
					rvStep = Double.parseDouble(rvStepField.getText());
				} else {
					rvStep = -1;
				}
				status = true;
				
				shell.dispose();
			}
		});

		buttonCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
        
		shell.pack();
		shell.open();
		shell.setSize(280, 180);
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
}
