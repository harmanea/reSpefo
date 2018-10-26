package cz.cuni.mff.respefo.Listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.IAxis;

import cz.cuni.mff.respefo.ChartBuilder;
import cz.cuni.mff.respefo.ReSpefo;

/**
 * Adds the default movement capabilities
 * 
 * Listens for:
 * 'w' / Arrow Up = Move the view up
 * 'a' / Arrow Left = Move the view left
 * 's' / Arrow Down = Move the view down
 * 'd' / Arrow Right = Move the view right
 * 
 * '+' = Zoom in
 * '-' = Zoom out
 * 
 * '8' / Numpad 8 = Zoom in Y Axis
 * '2' / Numpad 2 = Zoom Out Y Axis
 * '6' / Numpad 6 = Zoom in X Axis
 * '4' / Numpad 4 = Zoom Out X Axis
 * 
 * Space = Adjust the view so that all series are visible
 */
public class DefaultKeyListener extends KeyAdapter {
	protected Map<Integer, Consumer<KeyEvent>> handlers;
	
	public DefaultKeyListener() {
		handlers = new HashMap<>();
		
		handlers.put((int) 'w', DefaultKeyListener::moveUp);
		handlers.put(SWT.ARROW_UP, DefaultKeyListener::moveUp);
		
		handlers.put((int) 'a', DefaultKeyListener::moveLeft);
		handlers.put(SWT.ARROW_LEFT, DefaultKeyListener::moveLeft);
		
		handlers.put((int) 's', DefaultKeyListener::moveDown);
		handlers.put(SWT.ARROW_DOWN, DefaultKeyListener::moveDown);
		
		handlers.put((int) 'd', DefaultKeyListener::moveRight);
		handlers.put(SWT.ARROW_RIGHT, DefaultKeyListener::moveRight);
		
		handlers.put((int) '+', DefaultKeyListener::zoomIn);
		handlers.put(16777259, DefaultKeyListener::zoomIn);
		handlers.put(49, DefaultKeyListener::zoomIn);
		
		handlers.put((int) '-', DefaultKeyListener::zoomOut);
		handlers.put(16777261, DefaultKeyListener::zoomOut);
		handlers.put(47, DefaultKeyListener::zoomOut);
		
		handlers.put((int) '8', DefaultKeyListener::zoomInYAxis);
		handlers.put(SWT.KEYPAD_8, DefaultKeyListener::zoomInYAxis);
		
		handlers.put((int) '2', DefaultKeyListener::zoomOutYAxis);
		handlers.put(SWT.KEYPAD_2, DefaultKeyListener::zoomOutYAxis);
		
		handlers.put((int) '6', DefaultKeyListener::zoomInXAxis);
		handlers.put(SWT.KEYPAD_6, DefaultKeyListener::zoomInXAxis);
		
		handlers.put((int) '4', DefaultKeyListener::zoomOutXAxis);
		handlers.put(SWT.KEYPAD_4, DefaultKeyListener::zoomOutXAxis);
		
		handlers.put((int) SWT.SPACE, DefaultKeyListener::adjustView);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		Consumer<KeyEvent> handler = handlers.get(e.keyCode);
		if (handler != null) {
			handler.accept(e);
			
			if (ReSpefo.getChart() != null && !ReSpefo.getChart().isDisposed()) {
				ReSpefo.getChart().redraw();
			}
		}
	}

	private static void moveUp(KeyEvent e) {
		for (IAxis axis : ReSpefo.getChart().getAxisSet().getYAxes()) {
			axis.scrollUp();
		}
	}
	
	private static void moveDown(KeyEvent e) {
		for (IAxis axis : ReSpefo.getChart().getAxisSet().getYAxes()) {
			axis.scrollDown();
		}
	}
	
	private static void moveRight(KeyEvent e) {
		for (IAxis axis : ReSpefo.getChart().getAxisSet().getXAxes()) {
			axis.scrollUp();
		}
	}
	
	private static void moveLeft(KeyEvent e) {
		for (IAxis axis : ReSpefo.getChart().getAxisSet().getXAxes()) {
			axis.scrollDown();
		}
	}
	
	private static void zoomIn(KeyEvent e) {
		ReSpefo.getChart().getAxisSet().zoomIn();
	}
	
	private static void zoomOut(KeyEvent e) {
		ReSpefo.getChart().getAxisSet().zoomOut();
	}
	
	private static void zoomInYAxis(KeyEvent e) {
		for (IAxis axis : ReSpefo.getChart().getAxisSet().getYAxes()) {
			axis.zoomIn();
		}
	}
	
	private static void zoomOutYAxis(KeyEvent e) {
		for (IAxis axis : ReSpefo.getChart().getAxisSet().getYAxes()) {
			axis.zoomOut();
		}
	}
	
	private static void zoomInXAxis(KeyEvent e) {
		for (IAxis axis : ReSpefo.getChart().getAxisSet().getXAxes()) {
			axis.zoomIn();
		}
	}
	
	private static void zoomOutXAxis(KeyEvent e) {
		for (IAxis axis : ReSpefo.getChart().getAxisSet().getXAxes()) {
			axis.zoomOut();
		}
	}
	
	private static void adjustView(KeyEvent e) {
		ChartBuilder.adjustRange(ReSpefo.getChart());
	}
}
