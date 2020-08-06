package cz.cuni.mff.respefo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class CategoryDialog extends Dialog {
	private String value;
	
	public CategoryDialog(Shell parent) {
		super(parent, 0);
		
		value = null;
	}
	
	public String open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		shell.setText("Select category");
		
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.spacing = 5;
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		shell.setLayout(layout);

		Label label = new Label(shell, SWT.CENTER);
		label.setText("Category:");

		Combo combo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems("V", "R", "Ic");
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		Button buttonConfirm = new Button(composite, SWT.PUSH | SWT.CENTER);
		buttonConfirm.setText("Confirm");
		buttonConfirm.setEnabled(false);

		Button buttonCancel = new Button(composite, SWT.PUSH | SWT.CENTER);
		buttonCancel.setText("Cancel");
		
		shell.setDefaultButton(buttonConfirm);

		combo.addListener(SWT.Modify, event -> buttonConfirm.setEnabled(!combo.getText().equals("")));
		combo.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent event) {
				switch(event.character) {
				case 'v':
				case 'V':
					combo.setText("V");
					break;
				case 'r':
				case 'R':
					combo.setText("R");
					break;
				case 'i':
				case 'I':
					combo.setText("Ic");
					break;
				}
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {}
		});

		buttonConfirm.addListener(SWT.Selection, event -> {
			value = combo.getText();
			shell.close();
		});
		buttonCancel.addListener(SWT.Selection, event -> shell.close());

		shell.pack();
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return value;
	}
}
