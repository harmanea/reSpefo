package cz.cuni.mff;

public class Spectrum {
	private Point[] data;

	private double max_Y, min_Y;

	public Spectrum(Point[] data) {
		this.data = data;
		double max_Y = Double.MIN_VALUE;
		double min_Y = Double.MAX_VALUE;
		double Y;
		for (Point p : this.data) {
			Y = p.getY();
			if (Y > max_Y) {
				max_Y = Y;
			} else if (Y < min_Y) {
				min_Y = Y;
			}
		}
		this.max_Y = max_Y;
		this.min_Y = min_Y;
	}
	
	public int size() {
		return data.length;
	}

	public double getMaxX() {
		return data[data.length - 1].getX();
	}

	public double getMinX() {
		return data[0].getX();
	}

	public double getMaxY() {
		return max_Y;
	}

	public double getMinY() {
		return min_Y;
	}

	public double getX(int at) throws IndexOutOfBoundsException {
		if ((at >= 0) && (at < data.length)) {
			return data[at].getX();
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	public double getY(int at) throws IndexOutOfBoundsException {
		if ((at >= 0) && (at < data.length)) {
			return data[at].getY();
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	public Point[] getData() {
		return data;
	}
}
