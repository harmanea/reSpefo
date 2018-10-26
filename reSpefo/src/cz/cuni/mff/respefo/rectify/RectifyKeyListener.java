package cz.cuni.mff.respefo.rectify;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import cz.cuni.mff.respefo.Listeners.DefaultKeyListener;

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
		
		handlers.put((int) SWT.CR, RectifyKeyListener::enter); // Enter
		
		handlers.put((int) SWT.ESC, RectifyKeyListener::escape); // Escape
		
		handlers.put(SWT.INSERT, RectifyKeyListener::insert);
		
		handlers.put((int) SWT.DEL, RectifyKeyListener::delete); // Delete
		
		handlers.put((int) 'x', RectifyKeyListener::clearPoints);
	}
	
	public static void previousPoint(KeyEvent e) {
		RectifyItemListener.getInstance().previousPoint();
	}
	
	public static void nextPoint(KeyEvent e) {
		RectifyItemListener.getInstance().nextPoint();
	}
	
	public static void moveUp(KeyEvent e) {
		RectifyItemListener.getInstance().moveUp();
	}
	
	public static void moveDown(KeyEvent e) {
		RectifyItemListener.getInstance().moveDown();
	}
	
	public static void moveLeft(KeyEvent e) {
		RectifyItemListener.getInstance().moveLeft();
	}
	
	public static void moveRight(KeyEvent e) {
		RectifyItemListener.getInstance().moveRight();
	}
	
	public static void center(KeyEvent e) {
		RectifyItemListener.getInstance().center();
	}
	
	public static void enter(KeyEvent e) {
		RectifyItemListener.getInstance().enter();
	}
	
	public static void escape(KeyEvent e) {
		RectifyItemListener.getInstance().escape();
	}
	
	public static void insert(KeyEvent e) {
		RectifyItemListener.getInstance().insert();
	}
	
	public static void delete(KeyEvent e) {
		RectifyItemListener.getInstance().delete();
	}
	
	public static void clearPoints(KeyEvent e) {
		if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
			RectifyItemListener.getInstance().clearPoints();
		}
	}
}
