package cz.cuni.mff.respefo.listeners;


import org.eclipse.swt.events.KeyEvent;

import cz.cuni.mff.respefo.function.CompareItemListener;

public class CompareKeyListener extends DefaultKeyListener {
	public CompareKeyListener() {
		super();
		
		handlers.put((int) 'i', CompareKeyListener::up);
		
		handlers.put((int) 'j', CompareKeyListener::left);
		
		handlers.put((int) 'k', CompareKeyListener::down);
		
		handlers.put((int) 'l', CompareKeyListener::right);
	}
	
	public static void up(KeyEvent event) {
		CompareItemListener.getInstance().up();
	}
	
	public static void down(KeyEvent event) {
		CompareItemListener.getInstance().down();
	}
	
	public static void left(KeyEvent event) {
		CompareItemListener.getInstance().left();
	}
	
	public static void right(KeyEvent event) {
		CompareItemListener.getInstance().right();
	}
}
