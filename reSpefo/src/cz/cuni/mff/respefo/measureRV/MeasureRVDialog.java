package cz.cuni.mff.respefo.measureRV;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
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
	
	public boolean open() {
		Display display = parent.getDisplay();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Measure radial velocity");
		
		// Part one
		Composite comp1 = new Composite(shell, SWT.NONE);
        Label one = new Label(comp1, SWT.LEFT);
        one.setText("Select spectrum to measure");
        one.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        Text textOne = new Text(comp1, SWT.BORDER);
        textOne.setText("");
        Button buttonOne = new Button(comp1, SWT.PUSH | SWT.CENTER);
        buttonOne.setText("   Browse...   ");

        // Part two
        Composite comp2 = new Composite(shell, SWT.NONE);
        Label two = new Label(comp2, SWT.LEFT);
        two.setText("Select .stl file(s) with measurements");
        two.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        List listOne = new List(comp2, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        Composite buttonCompOne = new Composite(comp2, SWT.NONE);
        Button buttonTwoOne = new Button(buttonCompOne, SWT.PUSH | SWT.CENTER);
        buttonTwoOne.setText("Add");
        Button buttonTwoTwo = new Button(buttonCompOne, SWT.PUSH | SWT.CENTER);
        buttonTwoTwo.setText("Remove");

        // Part three
        Composite comp3 = new Composite(shell, SWT.NONE);
        Label three = new Label(comp3, SWT.LEFT);
        three.setText("Select .stl file(s) with corrections");
        three.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        List listTwo = new List(comp3, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

        Composite buttonCompTwo = new Composite(comp3, SWT.NONE);
        Button buttonThreeOne = new Button(buttonCompTwo, SWT.PUSH | SWT.CENTER);
        buttonThreeOne.setText("Add");
        Button buttonThreeTwo = new Button(buttonCompTwo, SWT.PUSH | SWT.CENTER);
        buttonThreeTwo.setText("Remove");
        
        // Part four
        Composite comp4 = new Composite(shell, SWT.NONE);
        Button buttonFourOne = new Button(comp4, SWT.PUSH | SWT.CENTER);
        buttonFourOne.setText("Ok");
        buttonFourOne.setEnabled(false);
        Button buttonFourTwo = new Button(comp4, SWT.PUSH | SWT.CENTER);
        buttonFourTwo.setText("  Cancel  ");
        

        // Layout stuff
        GridLayout layout = new GridLayout(1, false);
        layout.marginBottom = 15;
        layout.marginLeft = 15;
        layout.marginRight = 15;
        layout.marginTop = 15;
        layout.verticalSpacing = 10;
        shell.setLayout(layout);
        
        layout = new GridLayout(2, false);
        comp1.setLayout(layout);
        comp1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        comp2.setLayout(layout);
        comp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        comp3.setLayout(layout);
        comp3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        layout = new GridLayout(2, true);
        comp4.setLayout(layout);
        comp4.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));
        layout = new GridLayout(1, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonCompOne.setLayout(layout);
        buttonTwoOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        buttonTwoTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        layout = new GridLayout(1, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonCompTwo.setLayout(layout);
        buttonThreeOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        buttonThreeTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        textOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        buttonOne.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
        listOne.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        listTwo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        buttonCompOne.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
        buttonCompTwo.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
        buttonFourOne.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        buttonFourTwo.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        // Listeners
		buttonOne.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				String s = Util.openFileDialog(Util.SPECTRUM_LOAD);
				
				if (s != null) {
					textOne.setText(s);
					textOne.setSelection(textOne.getText().length());
					
					if (listOne.getItemCount() > 0) {
						buttonFourOne.setEnabled(true);
					}
				}
			}

		});
		
		buttonTwoOne.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				String s = Util.openFileDialog(Util.STL_LOAD);
				
				if (s != null) {
					listOne.add(s);
					
					if (!textOne.getText().equals("")) {
						buttonFourOne.setEnabled(true);
					}
				}
			}

		});
		
		buttonTwoTwo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				if (listOne.getSelectionIndex() != -1) {
					listOne.remove(listOne.getSelectionIndex());
					
					if (listOne.getItemCount() == 0) {
						buttonFourOne.setEnabled(false);
					}
				}
			}

		});
		
		buttonThreeOne.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				String s = Util.openFileDialog(Util.STL_LOAD);
				
				if (s != null) {
					listTwo.add(s);
				}
			}

		});
		
		buttonThreeTwo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				if (listTwo.getSelectionIndex() != -1) {
					listTwo.remove(listTwo.getSelectionIndex());
				}
			}

		});
		
		buttonFourOne.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				status = true;
				itemsOne = listOne.getItems();
				itemsTwo = listTwo.getItems();
				fileName = textOne.getText();
				shell.dispose();
			}
		});
		
		buttonFourTwo.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				status = false;
				shell.dispose();
			}
		});
		
		// Pack and open
		shell.pack();
		shell.open();
		shell.setSize(600, 700);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return status;
	}
}
