package cz.cuni.mff.respefo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

public class Scene extends Composite {
	List<SavedListener> untypedListeners;
	List<KeyListener> keyListeners;
	List<MouseWheelListener> mouseWheelListeners;
	
	private class SavedListener {
		public SavedListener(int eventType, Listener listener) {
			super();
			this.eventType = eventType;
			this.listener = listener;
		}
		
		int eventType;
		Listener listener;
	}

	public Scene(Composite parent, int style) {
		super(parent, style);
		
		untypedListeners = new ArrayList<>();
		keyListeners = new ArrayList<>();
		mouseWheelListeners = new ArrayList<>();
	}

	public void addSavedListener(int eventType, Listener listener) {
		addListener(eventType, listener);
		
		untypedListeners.add(new SavedListener(eventType, listener));
	}
	
	public void addSavedKeyListener(KeyListener listener) {
		addKeyListener(listener);
		
		keyListeners.add(listener);
	}
	
	public void addSavedMouseWheelListener(MouseWheelListener listener) {
		addMouseWheelListener(listener);
		
		mouseWheelListeners.add(listener);
	}
	
	public void removeSavedListeners() {
		for (SavedListener listener : untypedListeners) {
			removeListener(listener.eventType, listener.listener);
		}
		
		for (KeyListener listener : keyListeners) {
			removeKeyListener(listener);
		}
		
		for (MouseWheelListener listener : mouseWheelListeners) {
			removeMouseWheelListener(listener);
		}
		
		untypedListeners.clear();
		keyListeners.clear();
		mouseWheelListeners.clear();
	}
}
