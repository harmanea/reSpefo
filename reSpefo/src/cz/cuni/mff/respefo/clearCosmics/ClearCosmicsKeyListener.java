package cz.cuni.mff.respefo.clearCosmics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import cz.cuni.mff.respefo.Listeners.DefaultKeyListener;
import cz.cuni.mff.respefo.rectify.RectifyItemListener;

public class ClearCosmicsKeyListener extends DefaultKeyListener {
	
	public ClearCosmicsKeyListener() {
		super();
	
		handlers.put((int) SWT.DEL, ClearCosmicsKeyListener::delete);
		
		handlers.put(SWT.INSERT, ClearCosmicsKeyListener::insert);
		
		handlers.put((int) 'm', ClearCosmicsKeyListener::next);
		
		handlers.put((int) 'n', ClearCosmicsKeyListener::previous);
		
		handlers.put((int) SWT.CR, ClearCosmicsKeyListener::enter); // Enter
		
		handlers.put((int) SWT.ESC, ClearCosmicsKeyListener::escape); // Escape
	}

	public static void delete(KeyEvent e) {
		ClearCosmicsItemListener.getInstance().delete();
	}
	
	public static void insert(KeyEvent e) {
		ClearCosmicsItemListener.getInstance().insert();
	}
	
	public static void next(KeyEvent e) {
		ClearCosmicsItemListener.getInstance().nextPoint();
	}
	
	public static void previous(KeyEvent e) {
		ClearCosmicsItemListener.getInstance().previousPoint();
	}
	
	public static void enter(KeyEvent e) {
		ClearCosmicsItemListener.getInstance().enter();
	}
	
	public static void escape(KeyEvent e) {
		ClearCosmicsItemListener.getInstance().escape();
	}
}
