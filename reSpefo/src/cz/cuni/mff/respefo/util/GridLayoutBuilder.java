package cz.cuni.mff.respefo.util;

import org.eclipse.swt.layout.GridLayout;

public class GridLayoutBuilder extends LayoutBuilder<GridLayout> {
	
	private GridLayoutBuilder() {
		layout = new GridLayout();
	}
	
	private GridLayoutBuilder(int columns, boolean makeColumnsEqualWidth) {
		layout = new GridLayout(columns, makeColumnsEqualWidth);
	}
	
	public static GridLayoutBuilder gridLayout(int columns, boolean makeColumnsEqualWidth) {
		return new GridLayoutBuilder(columns, makeColumnsEqualWidth);
	}
	
	public static GridLayoutBuilder gridLayout() {
		return new GridLayoutBuilder();
	}
	
	public GridLayoutBuilder margins(int value) {
		return this.marginHeight(value).marginWidth(value);
	}
	
	public GridLayoutBuilder marginWidth(int value) {
		layout.marginWidth = value;
		
		return this;
	}
	
	public GridLayoutBuilder marginHeight(int value) {
		layout.marginHeight = value;
		
		return this;
	}
	
	public GridLayoutBuilder marginTop(int value) {
		layout.marginTop = value;
		
		return this;
	}
	
	public GridLayoutBuilder marginLeft(int value) {
		layout.marginLeft = value;
		
		return this;
	}
	
	public GridLayoutBuilder marginRight(int value) {
		layout.marginRight = value;
		
		return this;
	}
	
	public GridLayoutBuilder marginBottom(int value) {
		layout.marginBottom = value;
		
		return this;
	}
	
	public GridLayoutBuilder spacings(int value) {
		return this.verticalSpacing(value).horizontalSpacing(value);
	}
	
	public GridLayoutBuilder verticalSpacing(int value) {
		layout.verticalSpacing = value;
		
		return this;
	}
	
	public GridLayoutBuilder horizontalSpacing(int value) {
		layout.horizontalSpacing = value;
		
		return this;
	}
}
