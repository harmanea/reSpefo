package cz.cuni.mff.respefo.measureRV;

public class RVResult {
	double rV;
	double shift;
	double radius;
	String category;
	double l0;
	String name;
	String comment;
	int index;
	
	public RVResult(double rV, double shift, double radius, String category, double l0, String name, String comment, int index) {
		super();
		this.rV = rV;
		this.shift = shift;
		this.radius = radius;
		this.category = category;
		this.l0 = l0;
		this.name = name;
		this.comment = comment;
		this.index = index;
	}
	
	@Override
	public String toString() {
		String result = category;
		if (comment != null && !comment.equals("")) {
			result += " - (" + comment + ")";
		}
		return result;
	}
}
