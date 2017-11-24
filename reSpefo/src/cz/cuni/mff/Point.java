package cz.cuni.mff;

public class Point {
	private double X,Y;
	
	public Point(double X, double Y) {
		this.X = X;
		this.Y = Y;
	}
	
	public void setX(double value) {
		X = value;
	}
	
	public void setY(double value) {
		Y = value;
	}
	
	public double getX() {
		return X;
	}
	
	public double getY() {
		return Y;
	}
}
