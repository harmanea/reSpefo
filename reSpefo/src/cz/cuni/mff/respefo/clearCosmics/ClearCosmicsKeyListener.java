package cz.cuni.mff.respefo.clearCosmics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import cz.cuni.mff.respefo.Listeners.DefaultKeyListener;

public class ClearCosmicsKeyListener extends DefaultKeyListener {
	
	public ClearCosmicsKeyListener() {
		super();
	
		handlers.put((int) SWT.DEL, ClearCosmicsKeyListener::delete);
		
		handlers.put(SWT.INSERT, ClearCosmicsKeyListener::insert);
		
		handlers.put((int) 'm', ClearCosmicsKeyListener::next);
		
		handlers.put((int) 'n', ClearCosmicsKeyListener::previous);
		
		handlers.put((int) SWT.CR, ClearCosmicsKeyListener::enter);
		
		handlers.put((int) SWT.ESC, ClearCosmicsKeyListener::escape);
	}

	public static void delete(KeyEvent event) {
		ClearCosmicsItemListener.getInstance().delete();
	}
	
	public static void insert(KeyEvent event) {
		ClearCosmicsItemListener.getInstance().insert();
	}
	
	public static void next(KeyEvent event) {
		ClearCosmicsItemListener.getInstance().nextPoint();
	}
	
	public static void previous(KeyEvent event) {
		ClearCosmicsItemListener.getInstance().previousPoint();
	}
	
	public static void enter(KeyEvent event) {
		ClearCosmicsItemListener.getInstance().enter();
	}
	
	public static void escape(KeyEvent event) {
		ClearCosmicsItemListener.getInstance().escape();
	}
}
