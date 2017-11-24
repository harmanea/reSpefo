package cz.cuni.mff;

public class Spectrum {	
	private Point[] points;
	
	private double max_Y, min_Y;
	
	public int size() {
		return points.length;
	}
	
	public double getMaxX() {
		return points[points.length - 1].getX();
	}
	
	public double getMinX() {
		return points[0].getX();
	}
	
	public double getMaxY() {
		return max_Y;
	}
	
	public double getMinY() {
		return min_Y;
	}
	
	public double getX(int at) {
		if ((at >= 0) && (at < points.length)) {
			return points[at].getX();
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	public double getY(int at) {
		if ((at >= 0) && (at < points.length)) {
			return points[at].getY();
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
}
