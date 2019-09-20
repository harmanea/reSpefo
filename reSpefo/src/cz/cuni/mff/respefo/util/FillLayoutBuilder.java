package cz.cuni.mff.respefo.util;

import org.eclipse.swt.layout.FillLayout;

public class FillLayoutBuilder extends LayoutBuilder<FillLayout> {
	
	private FillLayoutBuilder() {
		layout = new FillLayout();
	}
	
	private FillLayoutBuilder(int type) {
		layout = new FillLayout(type);
	}
	
	public static FillLayoutBuilder fillLayout() {
		return new FillLayoutBuilder();
	}
	
	public static FillLayoutBuilder fillLayout(int type) {
		return new FillLayoutBuilder(type);
	}
	
	public FillLayoutBuilder spacing(int value) {
		layout.spacing = value;
		
		return this;
	}
	
	public FillLayoutBuilder margins(int value) {
		return this.marginWidth(value).marginHeight(value);
	}
	
	public FillLayoutBuilder marginWidth(int value) {
		layout.marginWidth = value;
		
		return this;
	}
	
	public FillLayoutBuilder marginHeight(int value) {
		layout.marginHeight = value;
		
		return this;
	}
}
