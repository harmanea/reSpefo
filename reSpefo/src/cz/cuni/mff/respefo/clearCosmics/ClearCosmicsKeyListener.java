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
	}

	public static void delete(KeyEvent e) {
		ClearCosmicsItemListener.getInstance().delete();
	}
	
	public static void insert(KeyEvent e) {
		ClearCosmicsItemListener.getInstance().insert();
	}
}
