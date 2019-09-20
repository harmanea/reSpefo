package cz.cuni.mff.respefo.util;

import org.eclipse.swt.layout.RowLayout;

public class RowLayoutBuilder extends LayoutBuilder<RowLayout> {
	
	private RowLayoutBuilder() {
		layout = new RowLayout();
	}
	
	private RowLayoutBuilder(int type) {
		layout = new RowLayout(type);
	}
	
	public static RowLayoutBuilder rowLayout() {
		return new RowLayoutBuilder();
	}
	
	public static RowLayoutBuilder rowLayout(int type) {
		return new RowLayoutBuilder(type);
	}
	
	public RowLayoutBuilder margins(int value) {
		return this.marginHeight(value).marginWidth(value);
	}
	
	public RowLayoutBuilder marginWidth(int value) {
		layout.marginWidth = value;

		return this;
	}
	
	public RowLayoutBuilder marginHeight(int value) {
		layout.marginHeight = value;
		
		return this;
	}
	
	public RowLayoutBuilder marginTop(int value) {
		layout.marginTop = value;
		
		return this;
	}
	
	public RowLayoutBuilder marginLeft(int value) {
		layout.marginLeft = value;
		
		return this;
	}
	
	public RowLayoutBuilder marginRight(int value) {
		layout.marginRight = value;
		
		return this;
	}
	
	public RowLayoutBuilder marginBottom(int value) {
		layout.marginBottom = value;
		
		return this;
	}
	
	public RowLayoutBuilder spacing(int value) {
		layout.spacing = value;
		
		return this;
	}
	
	public RowLayoutBuilder center(boolean value) {
		layout.center = value;
		
		return this;
	}
	
	public RowLayoutBuilder fill(boolean value) {
		layout.fill = value;
		
		return this;
	}
	
	public RowLayoutBuilder justify(boolean value) {
		layout.justify = value;
		
		return this;
	}
	
	public RowLayoutBuilder pack(boolean value) {
		layout.pack = value;
		
		return this;
	}
	
	public RowLayoutBuilder wrap(boolean value) {
		layout.wrap = value;
		
		return this;
	}
}
