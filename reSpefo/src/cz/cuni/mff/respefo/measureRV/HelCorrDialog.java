package cz.cuni.mff.respefo.measureRV;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.cuni.mff.respefo.Util;

class HelCorrDialog extends Dialog {
	private Shell parent;
	private boolean status;

	public HelCorrDialog(Shell parent) {
		super(parent, 0);
		
		this.parent = parent;
		status = false;
	}

	public boolean open() {
		Display display = parent.getDisplay();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Rectify");
		
		// Part one
		Composite compTop = new Composite(shell, SWT.NONE);
		
        Label one = new Label(compTop, SWT.LEFT);
        one.setText("Select spectrum to rectify");
        one.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        
        Text textOne = new Text(compTop, SWT.BORDER);
        textOne.setText("");
        
        Button buttonOne = new Button(compTop, SWT.PUSH | SWT.CENTER);
        buttonOne.setText("   Browse...   ");
        
        
        // Part two
        Group group = new Group(shell, SWT.RADIO);
        group.setText("Heliocentric correction");
        
        Composite compGroupOne = new Composite(group, SWT.NONE);
        
        Button buttonRadioOne = new Button(compGroupOne, SWT.RADIO);
        buttonRadioOne.setText("Absolute value:");
        buttonRadioOne.setSelection(true);
        
        Text textTwo = new Text(compGroupOne, SWT.BORDER);
        textTwo.setText("0");
        
        
        Composite compGroupTwo = new Composite(group, SWT.NONE);
        
        Button buttonRadioTwo = new Button(compGroupTwo, SWT.RADIO);
        buttonRadioTwo.setText("Get from .lst file:");
        
        Text textThree = new Text(compGroupTwo, SWT.BORDER);
        textThree.setText("");
        
        Button buttonTwo = new Button(compGroupTwo, SWT.PUSH | SWT.CENTER);
        buttonTwo.setText("   Browse...   ");
        
        
        Composite compGroupThree = new Composite(group, SWT.NONE);
        
        Button buttonRadioThree = new Button(compGroupThree, SWT.RADIO);
        buttonRadioThree.setText("Extract from FITS header.");
        
        
        // Part three
        Composite compOkCancel = new Composite(shell, SWT.NONE);
        Button buttonOk = new Button(compOkCancel, SWT.PUSH | SWT.CENTER);
        buttonOk.setText("Ok");
        buttonOk.setEnabled(false);
        Button buttonCancel = new Button(compOkCancel, SWT.PUSH | SWT.CENTER);
        buttonCancel.setText("  Cancel  ");
		
        // Layout stuff
        GridLayout layout = new GridLayout(1, false);
        layout.marginBottom = 15;
        layout.marginLeft = 15;
        layout.marginRight = 15;
        layout.marginTop = 15;
        layout.verticalSpacing = 10;
        shell.setLayout(layout);
        
        layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        compTop.setLayout(layout);
        compTop.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        layout = new GridLayout(1, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        layout = new GridLayout(2, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
		compOkCancel.setLayout(layout);
		compOkCancel.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));
		
		layout = new GridLayout(2, false);
		compGroupOne.setLayout(layout);
		compGroupOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		layout = new GridLayout(3, false);
		compGroupTwo.setLayout(layout);
		compGroupTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		layout = new GridLayout(1, false);
		compGroupThree.setLayout(layout);
		compGroupThree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		textOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonOne.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));

		buttonRadioOne.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		textTwo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buttonRadioTwo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		textThree.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		buttonTwo.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		buttonRadioThree.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		buttonOk.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		buttonCancel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
		// Listeners
		buttonOne.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				String s = Util.openFileDialog(Util.SPECTRUM_LOAD);

				if (s != null) {
					textOne.setText(s);
					textOne.setSelection(textOne.getText().length());
				}
			}

		});
		
		buttonTwo.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				String s = Util.openFileDialog(Util.LST_LOAD);

				if (s != null) {
					textThree.setText(s);
					textThree.setSelection(textThree.getText().length());
				}
			}
		});
		
		buttonRadioOne.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonRadioTwo.setSelection(false);
				buttonRadioThree.setSelection(false);
			}
		});
		
		buttonRadioTwo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonRadioOne.setSelection(false);
				buttonRadioThree.setSelection(false);
			}
		});
		
		buttonRadioThree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonRadioOne.setSelection(false);
				buttonRadioTwo.setSelection(false);
			}
		});
		
		buttonOk.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				status = true;
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
		shell.pack();
		shell.open();
		shell.setSize(600, 350); // TODO adjust accordingly
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
}
