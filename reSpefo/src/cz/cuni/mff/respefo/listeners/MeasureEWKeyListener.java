package cz.cuni.mff.respefo.listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import cz.cuni.mff.respefo.function.MeasureEWItemListener;

public class MeasureEWKeyListener extends DefaultKeyListener {

	public MeasureEWKeyListener() {
		super();
		
		handlers.put((int) SWT.SPACE, MeasureEWKeyListener::adjustView);
		
		handlers.put((int) 'a', MeasureEWKeyListener::moveLeft);
		handlers.put(SWT.ARROW_LEFT, MeasureEWKeyListener::moveLeft);
		
		handlers.put((int) 'd', MeasureEWKeyListener::moveRight);
		handlers.put(SWT.ARROW_RIGHT, MeasureEWKeyListener::moveRight);
				
		handlers.put(SWT.INSERT, MeasureEWKeyListener::add);
		handlers.put((int) SWT.CR, MeasureEWKeyListener::add);
		
		handlers.put((int) SWT.ESC, MeasureEWKeyListener::skip);
		handlers.put(SWT.END, MeasureEWKeyListener::skip);
		
		handlers.put((int) SWT.BS, MeasureEWKeyListener::remove);
		handlers.put((int) SWT.DEL, MeasureEWKeyListener::remove);
	}
	
	public static void moveLeft(KeyEvent event) {
		MeasureEWItemListener.getInstance().moveLeft();
	}
	
	public static void moveRight(KeyEvent event) {
		MeasureEWItemListener.getInstance().moveRight();
	}
	
	public static void add(KeyEvent event) {
		MeasureEWItemListener.getInstance().add();
	}
	
	public static void skip(KeyEvent event) {
		MeasureEWItemListener.getInstance().skip();
	}
	
	public static void remove(KeyEvent event) {
		MeasureEWItemListener.getInstance().remove();
	}
	
	public static void adjustView(KeyEvent event) {
		MeasureEWItemListener.getInstance().adjustView();
	}
}
