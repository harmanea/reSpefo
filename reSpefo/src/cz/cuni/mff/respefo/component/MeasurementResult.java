package cz.cuni.mff.respefo.component;

import java.util.ArrayList;
import java.util.List;

public class MeasurementResult {
	private int left;
	private int right;
	private List<Integer> points;
	private List<String> categories;
	
	public MeasurementResult(int left, int right) {
		points = new ArrayList<>();
		categories = new ArrayList<>();
		
		this.left = left;
		this.right = right;
	}

	public int getLeft() {
		return left;
	}
	
	public void moveLeft(int shift) {
		left += shift;
	}
	
	public void setLeft(int value) {
		left = value;
	}

	public int getRight() {
		return right;
	}
	
	public void moveRight(int shift) {
		right += shift;
	}
	
	public void setRight(int value) {
		right = value;
	}

	public List<Integer> getPoints() {
		return points;
	}

	public List<String> getCategories() {
		return categories;
	}

	public int size() {
		return points.size();
	}
}
