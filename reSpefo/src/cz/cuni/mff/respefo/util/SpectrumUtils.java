package cz.cuni.mff.respefo.util;

import java.util.ArrayList;

import cz.cuni.mff.respefo.component.SeriesSet;

public class SpectrumUtils {
	public static SeriesSet transformToEquidistant(double[] xSeries, double[] ySeries) {
		double deltaRV = ((xSeries[1] - xSeries[0]) * MathUtils.SPEED_OF_LIGHT) / (xSeries[0] * 3);
		
		return transformToEquidistant(xSeries, ySeries, deltaRV);
	}
	
	public static SeriesSet transformToEquidistant(double[] xSeries, double[] ySeries, double deltaRV) {
		ArrayList<Double> xList = new ArrayList<>();
		xList.add(xSeries[0]);
		while (xList.get(xList.size() - 1) < xSeries[xSeries.length - 1]) {
			xList.add(xList.get(xList.size() - 1) * (1 + deltaRV / MathUtils.SPEED_OF_LIGHT));
		}
		
		double[] newXSeries = xList.stream().mapToDouble(Double::doubleValue).toArray();
		double[] newYSeries = MathUtils.intep(xSeries, ySeries, newXSeries);
		
		return new SeriesSet(newXSeries, newYSeries);
	}
	
}
