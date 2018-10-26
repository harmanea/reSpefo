package cz.cuni.mff.respefo.measureRV;

class RVMeasurement {
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
}
