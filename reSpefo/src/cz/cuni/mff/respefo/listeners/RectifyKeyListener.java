package cz.cuni.mff.respefo.listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import cz.cuni.mff.respefo.function.RectifyItemListener;

public class RectifyKeyListener extends DefaultKeyListener {
	public RectifyKeyListener() {
		super();
		
		handlers.put((int) 'n', RectifyKeyListener::previousPoint);
		
		handlers.put((int) 'm', RectifyKeyListener::nextPoint);
		
		handlers.put((int) 'i', RectifyKeyListener::moveUp);
		handlers.put((int) 'k', RectifyKeyListener::moveDown);
		handlers.put((int) 'j', RectifyKeyListener::moveLeft);
		handlers.put((int) 'l', RectifyKeyListener::moveRight);
		
		handlers.put((int) 'p', RectifyKeyListener::center);
		
		handlers.put((int) SWT.CR, RectifyKeyListener::enter);
		
		handlers.put((int) SWT.ESC, RectifyKeyListener::escape);
		
		handlers.put(SWT.INSERT, RectifyKeyListener::insert);
		
		handlers.put((int) SWT.DEL, RectifyKeyListener::delete);
		
		handlers.put((int) 'x', RectifyKeyListener::clearPoints);
		
		handlers.put((int) SWT.TAB, RectifyKeyListener::tab);
	}
	
	public static void previousPoint(KeyEvent event) {
		RectifyItemListener.getInstance().previousPoint();
	}
	
	public static void nextPoint(KeyEvent event) {
		RectifyItemListener.getInstance().nextPoint();
	}
	
	public static void moveUp(KeyEvent event) {
		RectifyItemListener.getInstance().moveUp();
	}
	
	public static void moveDown(KeyEvent event) {
		RectifyItemListener.getInstance().moveDown();
	}
	
	public static void moveLeft(KeyEvent event) {
		RectifyItemListener.getInstance().moveLeft();
	}
	
	public static void moveRight(KeyEvent event) {
		RectifyItemListener.getInstance().moveRight();
	}
	
	public static void center(KeyEvent event) {
		RectifyItemListener.getInstance().center();
	}
	
	public static void enter(KeyEvent event) {
		RectifyItemListener.getInstance().enter();
	}
	
	public static void escape(KeyEvent event) {
		RectifyItemListener.getInstance().escape();
	}
	
	public static void insert(KeyEvent event) {
		RectifyItemListener.getInstance().insert();
	}
	
	public static void delete(KeyEvent event) {
		RectifyItemListener.getInstance().delete();
	}
	
	public static void clearPoints(KeyEvent event) {
		if ((event.stateMask & SWT.CTRL) == SWT.CTRL) {
			RectifyItemListener.getInstance().clearPoints();
		}
	}
	
	public static void tab(KeyEvent event) {
		RectifyItemListener.getInstance().tab();
	}
}
