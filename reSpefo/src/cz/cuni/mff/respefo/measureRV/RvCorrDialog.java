package cz.cuni.mff.respefo.measureRV;

import static cz.cuni.mff.respefo.measureRV.RvCorrection.BARYCENTRIC;
import static cz.cuni.mff.respefo.measureRV.RvCorrection.HELIOCENTRIC;
import static cz.cuni.mff.respefo.measureRV.RvCorrection.UNDEFINED;

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

public class RvCorrDialog extends Dialog {
	private double value;
	private int type;
	
	private Composite compOne, compTwo, compThree;
	private Label labelOne, labelTwo, labelThree;
	private Button helCorrButton, barCorrButton, noneButton, confirmButton;
	private Text helCorrText, barCorrText;
	
	private boolean status = false;

	public RvCorrDialog(Shell parent) {
		super(parent);
		
		value = Double.NaN;
		type = UNDEFINED;
	}
	
	public double getValue() {
		return value;
	}
	
	public int getType() {
		return type;
	}

	public boolean open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("RV Correction");
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		shell.setLayout(layout);
		
		labelOne = new Label(shell, SWT.LEFT | SWT.WRAP);
		labelOne.setText("No rv correction information was gathered from the spectrum file. Please insert it manually.");
		labelOne.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		compOne = new Composite(shell, SWT.NONE);
		layout = new GridLayout(3, false);
		compOne.setLayout(layout);
		compOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		helCorrButton = new Button(compOne, SWT.RADIO);
		helCorrButton.setText("Heliocentric correction:");
		helCorrButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		helCorrButton.addListener(SWT.Selection, event -> selectRadioButton(helCorrButton));
		
		helCorrText = new Text(compOne, SWT.RIGHT);
		helCorrText.setText("0");
		helCorrText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		helCorrText.setEnabled(false);
		helCorrText.addListener(SWT.Modify, event -> verifyAndSetText(helCorrText));
		
		labelTwo = new Label(compOne, SWT.RIGHT);
		labelTwo.setText("km/s");
		labelTwo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true));
		
		compTwo = new Composite(shell, SWT.NONE);
		layout = new GridLayout(3, false);
		compTwo.setLayout(layout);
		compTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		barCorrButton = new Button(compTwo, SWT.RADIO);
		barCorrButton.setText("Barycentric correction:");
		barCorrButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		barCorrButton.addListener(SWT.Selection, event -> selectRadioButton(barCorrButton));
		
		barCorrText = new Text(compTwo, SWT.RIGHT);
		barCorrText.setText("0");
		barCorrText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		barCorrText.setEnabled(false);
		barCorrText.addListener(SWT.Modify, event -> verifyAndSetText(barCorrText));
		
		labelThree = new Label(compTwo, SWT.RIGHT);
		labelThree.setText("km/s");
		labelThree.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true));
		
		compThree = new Composite(shell, SWT.NONE);
		layout = new GridLayout(1, false);
		compThree.setLayout(layout);
		compThree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		noneButton = new Button(compThree, SWT.RADIO);
		noneButton.setText("Do not specify.");
		noneButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		noneButton.setSelection(true);
		noneButton.addListener(SWT.Selection, event -> selectRadioButton(noneButton));
		
		confirmButton = new Button(shell, SWT.PUSH);
		confirmButton.setText("Confirm");
		confirmButton.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
		confirmButton.addListener(SWT.Selection, event -> setStatusAndCloseShell(true, shell));
		
		shell.setDefaultButton(confirmButton);
		
		shell.pack();
		shell.open();
		shell.setSize(380, 230);
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
	
	private void selectRadioButton(Button selected) {
		if (selected == helCorrButton) {
			helCorrText.setEnabled(true);
			
			barCorrButton.setSelection(false);
			barCorrText.setEnabled(false);
			
			noneButton.setSelection(false);
			
			verifyAndSetText(helCorrText);
			
			type = HELIOCENTRIC;
		} else if (selected == barCorrButton) {
			barCorrText.setEnabled(true);
			
			helCorrButton.setSelection(false);
			helCorrText.setEnabled(false);
			
			noneButton.setSelection(false);
			
			verifyAndSetText(barCorrText);
			
			type = BARYCENTRIC;
		} else if (selected == noneButton) {
			helCorrButton.setSelection(false);
			helCorrText.setEnabled(false);
			
			barCorrButton.setSelection(false);
			barCorrText.setEnabled(false);
			
			confirmButton.setEnabled(true);
			
			type = UNDEFINED;
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
