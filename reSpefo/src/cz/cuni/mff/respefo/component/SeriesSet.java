package cz.cuni.mff.respefo.component;

public class SeriesSet {
	private double[] xSeries;
	private double[] ySeries;
	
	public SeriesSet(double[] xSeries, double[] ySeries) {
		super();
		this.xSeries = xSeries;
		this.ySeries = ySeries;
	}
	
	public double[] getXSeries() {
		return xSeries;
	}
	
	public void setXSeries(double[] xSeries) {
		this.xSeries = xSeries;
	}
	
	public double[] getYSeries() {
		return ySeries;
	}
	
	public void setYSeries(double[] ySeries) {
		this.ySeries = ySeries;
	}
}
