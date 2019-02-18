package cz.cuni.mff.respefo.measureRV;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Listeners.DefaultKeyListener;

public class MeasureRVKeyListener extends DefaultKeyListener {
	
	public MeasureRVKeyListener() {
		super();
		
		handlers.put((int) SWT.SPACE, MeasureRVKeyListener::adjustView);
		
		handlers.put((int) 'a', MeasureRVKeyListener::moveLeft);
		handlers.put(SWT.ARROW_LEFT, MeasureRVKeyListener::moveLeft);
		
		handlers.put((int) 'd', MeasureRVKeyListener::moveRight);
		handlers.put(SWT.ARROW_RIGHT, MeasureRVKeyListener::moveRight);
		
		handlers.put((int) '+', MeasureRVKeyListener::zoomIn);
		handlers.put(16777259, MeasureRVKeyListener::zoomIn);
		handlers.put(49, MeasureRVKeyListener::zoomIn);
		
		handlers.put((int) '-', MeasureRVKeyListener::zoomOut);
		handlers.put(16777261, MeasureRVKeyListener::zoomOut);
		handlers.put(47, MeasureRVKeyListener::zoomOut);
		
		handlers.put((int) SWT.CR, MeasureRVKeyListener::enter); // Enter
		
		handlers.put(SWT.INSERT, MeasureRVKeyListener::insert);
		
		handlers.put((int) SWT.ESC, MeasureRVKeyListener::skip); // Escape
		handlers.put(SWT.END, MeasureRVKeyListener::skip); // End
		
		handlers.put((int) SWT.BS, MeasureRVKeyListener::delete); // Backspace
		handlers.put((int) SWT.DEL, MeasureRVKeyListener::delete); // Delete
		
		handlers.put((int) SWT.TAB, MeasureRVKeyListener::changeRadius);
	}
	
	public static void adjustView(KeyEvent e) {
		ChartBuilder.adjustRange(ReSpefo.getChart(), 1, 1);
		ReSpefo.getChart().getAxisSet().zoomOut();
		MeasureRVItemListener.getInstance().adjustRVStepLabel();
	}
	
	public static void moveRight(KeyEvent e) {
		MeasureRVItemListener.getInstance().moveRight();
	}
	
	public static void moveLeft(KeyEvent e) {
		MeasureRVItemListener.getInstance().moveLeft();
	}
	
	public static void zoomIn(KeyEvent e) {
		ReSpefo.getChart().getAxisSet().zoomIn();
		MeasureRVItemListener.getInstance().adjustRVStepLabel();
	}
	
	public static void zoomOut(KeyEvent e) {
		ReSpefo.getChart().getAxisSet().zoomOut();
		MeasureRVItemListener.getInstance().adjustRVStepLabel();
	}
	
	public static void enter(KeyEvent e) {
		MeasureRVItemListener.getInstance().enter();
	}
	
	public static void insert(KeyEvent e) {
		MeasureRVItemListener.getInstance().insert();
	}
	
	public static void skip(KeyEvent e) {
		MeasureRVItemListener.getInstance().skip();
	}
	
	public static void delete(KeyEvent e) {
		MeasureRVItemListener.getInstance().delete();
	}
	
	public static void changeRadius(KeyEvent e) {
		if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
			MeasureRVItemListener.getInstance().decreaseRadius();
		} else {
			MeasureRVItemListener.getInstance().increaseRadius();
		}
	}
}
