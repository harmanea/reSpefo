package cz.cuni.mff.respefo.dialog;

import static cz.cuni.mff.respefo.component.RvCorrection.BARYCENTRIC;
import static cz.cuni.mff.respefo.component.RvCorrection.HELIOCENTRIC;
import static cz.cuni.mff.respefo.component.RvCorrection.UNDEFINED;

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

import cz.cuni.mff.respefo.component.RvCorrection;

public class RvCorrDialog extends Dialog {
	private double value;
	private int type;
	private boolean applyCorrection;
	
	private Button noneButton, appliedButton, notAppliedButton, helCorrButton, barCorrButton, confirmButton;
	private Text corrText;
	
	private boolean status = false;

	public RvCorrDialog(Shell parent) {
		super(parent);
		
		value = Double.NaN;
		type = UNDEFINED;
		applyCorrection = false;
	}
	
	public RvCorrection getCorrection() {
		return new RvCorrection(type, value);
	}
	
	public boolean applyCorrection() {
		return applyCorrection;
	}

	public boolean open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("RV Correction");
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		shell.setLayout(layout);
		
		Label labelOne = new Label(shell, SWT.LEFT | SWT.WRAP);
		labelOne.setText("No rv correction information was gathered from the spectrum file. Please insert it manually.");
		labelOne.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		
		Composite compOne = new Composite(shell, SWT.NONE);
		layout = new GridLayout(3, true);
		compOne.setLayout(layout);
		compOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		noneButton = new Button(compOne, SWT.TOGGLE);
		noneButton.setText("Do not specify");
		noneButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		noneButton.setSelection(true);
		noneButton.setEnabled(false);
		noneButton.addListener(SWT.Selection, event -> selectTopButton(noneButton));
		
		appliedButton = new Button(compOne, SWT.TOGGLE);
		appliedButton.setText("Correction applied");
		appliedButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		appliedButton.setSelection(false);
		appliedButton.addListener(SWT.Selection, event -> selectTopButton(appliedButton));
		
		notAppliedButton = new Button(compOne, SWT.TOGGLE);
		notAppliedButton.setText("Apply correction");
		notAppliedButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		notAppliedButton.setSelection(false);
		notAppliedButton.addListener(SWT.Selection, event -> selectTopButton(notAppliedButton));
		
		
		Composite compTwo = new Composite(shell, SWT.NONE);
		layout = new GridLayout(2, true);
		compTwo.setLayout(layout);
		compTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		helCorrButton = new Button(compTwo, SWT.TOGGLE);
		helCorrButton.setText("Heliocentric correction");
		helCorrButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		helCorrButton.setSelection(true);
		helCorrButton.setEnabled(false);
		helCorrButton.addListener(SWT.Selection, event -> selectBottomButton(helCorrButton));
		
		barCorrButton = new Button(compTwo, SWT.TOGGLE);
		barCorrButton.setText("Barycentric correction");
		barCorrButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		barCorrButton.setSelection(false);
		barCorrButton.setEnabled(false);
		barCorrButton.addListener(SWT.Selection, event -> selectBottomButton(barCorrButton));
		
		
		Composite compThree = new Composite(shell, SWT.NONE);
		layout = new GridLayout(3, false);
		compThree.setLayout(layout);
		compThree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label labelTwo = new Label(compThree, SWT.LEFT);
		labelTwo.setText("Value:");
		labelTwo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		corrText = new Text(compThree, SWT.BORDER);
		corrText.setText("0");
		corrText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		corrText.setEnabled(false);
		corrText.addListener(SWT.Modify, event -> verifyAndSetText(corrText));
		
		Label labelThree = new Label(compThree, SWT.RIGHT);
		labelThree.setText("km/s");
		labelThree.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		
		confirmButton = new Button(shell, SWT.PUSH);
		confirmButton.setText("Confirm");
		confirmButton.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
		confirmButton.addListener(SWT.Selection, event -> setStatusAndCloseShell(true, shell));
		
		shell.setDefaultButton(confirmButton);
		
		shell.pack();
		shell.open();
		shell.setSize(500, 300);
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
	
	private void selectTopButton(Button selected) {
		selected.setEnabled(false);
		applyCorrection = notAppliedButton.getSelection();
		
		if (selected == noneButton) {
			appliedButton.setSelection(false);
			appliedButton.setEnabled(true);
			notAppliedButton.setSelection(false);
			notAppliedButton.setEnabled(true);
			
			helCorrButton.setEnabled(false);
			barCorrButton.setEnabled(false);
			
			corrText.setEnabled(false);
			
			confirmButton.setEnabled(true);
			
			type = UNDEFINED;
		} else if (selected == appliedButton) {
			noneButton.setSelection(false);
			noneButton.setEnabled(true);
			notAppliedButton.setSelection(false);
			notAppliedButton.setEnabled(true);
			
			helCorrButton.setEnabled(!helCorrButton.getSelection());
			barCorrButton.setEnabled(!barCorrButton.getSelection());
			
			corrText.setEnabled(true);	
			verifyAndSetText(corrText);
			
			type = helCorrButton.getSelection() ? HELIOCENTRIC : BARYCENTRIC;
		} else if (selected == notAppliedButton) {
			noneButton.setSelection(false);
			noneButton.setEnabled(true);
			appliedButton.setSelection(false);
			appliedButton.setEnabled(true);
			
			helCorrButton.setEnabled(!helCorrButton.getSelection());
			barCorrButton.setEnabled(!barCorrButton.getSelection());
			
			corrText.setEnabled(true);
			verifyAndSetText(corrText);
			
			type = helCorrButton.getSelection() ? HELIOCENTRIC : BARYCENTRIC;
		}
	}
	
	private void selectBottomButton(Button selected) {
		selected.setEnabled(false);
		
		if (selected == helCorrButton) {
			barCorrButton.setSelection(false);
			barCorrButton.setEnabled(true);
			
			type = HELIOCENTRIC;
		} else if (selected == barCorrButton) {
			helCorrButton.setSelection(false);
			helCorrButton.setEnabled(true);
			
			type = BARYCENTRIC;
		}
	}
	
	private void verifyAndSetText(Text text) {
		try {
			double parsedValue = Double.parseDouble(text.getText());
			
			value = parsedValue;
			confirmButton.setEnabled(true);
		} catch (NumberFormatException numberFormatException) {
			value = Double.NaN;
			
			confirmButton.setEnabled(false);
		}
	}
	
	private void setStatusAndCloseShell(boolean status, Shell shell) {
		this.status = status;
		shell.close();
	}
}
