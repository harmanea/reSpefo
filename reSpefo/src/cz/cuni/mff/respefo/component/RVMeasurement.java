package cz.cuni.mff.respefo.component;

public class RVMeasurement {
	double l0;
	double radius;
	String name;
	boolean isCorrection;
	
	public RVMeasurement(double l0, double radius, String name, boolean isCorrection) {
		this.l0 = l0;
		this.radius = radius;
		this.name = name;
		this.isCorrection = isCorrection;
	}
	
	public double getL0() {
		return l0;
	}

	public double getRadius() {
		return radius;
	}

	public String getName() {
		return name;
	}

	public boolean isCorrection() {
		return isCorrection;
	}

	public void setL0(double l0) {
		this.l0 = l0;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCorrection(boolean isCorrection) {
		this.isCorrection = isCorrection;
	}
	
	public void increaseRadius() {
		this.radius *= 1.5;
	}
	
	public void decreaseRadius() {
		this.radius /= 1.5;
	}
}
