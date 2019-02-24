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
		
		handlers.put((int) SWT.CR, MeasureRVKeyListener::enter);
		
		handlers.put(SWT.INSERT, MeasureRVKeyListener::insert);
		
		handlers.put((int) SWT.ESC, MeasureRVKeyListener::skip);
		handlers.put(SWT.END, MeasureRVKeyListener::skip);
		
		handlers.put((int) SWT.BS, MeasureRVKeyListener::delete);
		handlers.put((int) SWT.DEL, MeasureRVKeyListener::delete);
		
		handlers.put((int) SWT.TAB, MeasureRVKeyListener::changeRadius);
	}
	
	public static void adjustView(KeyEvent event) {
		ChartBuilder.adjustRange(ReSpefo.getChart(), 1, 1);
		ReSpefo.getChart().getAxisSet().zoomOut();
		MeasureRVItemListener.getInstance().adjustRVStepLabel();
	}
	
	public static void moveRight(KeyEvent event) {
		MeasureRVItemListener.getInstance().moveRight();
	}
	
	public static void moveLeft(KeyEvent event) {
		MeasureRVItemListener.getInstance().moveLeft();
	}
	
	public static void zoomIn(KeyEvent event) {
		ReSpefo.getChart().getAxisSet().zoomIn();
		MeasureRVItemListener.getInstance().adjustRVStepLabel();
	}
	
	public static void zoomOut(KeyEvent event) {
		ReSpefo.getChart().getAxisSet().zoomOut();
		MeasureRVItemListener.getInstance().adjustRVStepLabel();
	}
	
	public static void enter(KeyEvent event) {
		MeasureRVItemListener.getInstance().enter();
	}
	
	public static void insert(KeyEvent event) {
		MeasureRVItemListener.getInstance().insert();
	}
	
	public static void skip(KeyEvent event) {
		MeasureRVItemListener.getInstance().skip();
	}
	
	public static void delete(KeyEvent event) {
		MeasureRVItemListener.getInstance().delete();
	}
	
	public static void changeRadius(KeyEvent event) {
		if ((event.stateMask & SWT.CTRL) == SWT.CTRL) {
			MeasureRVItemListener.getInstance().decreaseRadius();
		} else {
			MeasureRVItemListener.getInstance().increaseRadius();
		}
	}
}
