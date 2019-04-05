package cz.cuni.mff.respefo.component;

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
	
	public double getrV() {
		return rV;
	}

	public double getShift() {
		return shift;
	}

	public double getRadius() {
		return radius;
	}

	public String getCategory() {
		return category;
	}

	public double getL0() {
		return l0;
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public int getIndex() {
		return index;
	}

	public void setrV(double rV) {
		this.rV = rV;
	}

	public void setShift(double shift) {
		this.shift = shift;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setL0(double l0) {
		this.l0 = l0;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setIndex(int index) {
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
