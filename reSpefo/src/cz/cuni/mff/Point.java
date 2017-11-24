package cz.cuni.mff;

public class Point {
	private double X, Y;

	/**
	 * 
	 * @param X value to be set as X
	 * @param Y value to be set as Y
	 */
	public Point(double X, double Y) {
		this.X = X;
		this.Y = Y;
	}
	
	/**
	 * Sets the Point's X coordinate
	 * 
	 * @param {@code double} value to be set as X
	 */
	public void setX(double value) {
		X = value;
	}

	/**
	 * Sets the Point's Y coordinate
	 * 
	 * @param {@code double} value to be set as Y
	 */
	public void setY(double value) {
		Y = value;
	}

	/**
	 * Gets the Point's X coordinate
	 * 
	 * @return {@code double} value of X
	 */
	public double getX() {
		return X;
	}

	/**
	 * Gets the Point's Y coordinate
	 * 
	 * @return {@code double} value of Y
	 */
	public double getY() {
		return Y;
	}
}