package cz.cuni.mff.respefo.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.cuni.mff.respefo.util.ArrayUtils;

public class MeasurementResults implements Iterable<MeasurementResult> {
	private List<MeasurementResult> results;

	public MeasurementResults(Measurements measurements, double[] xSeries) {
		results = new ArrayList<>();
		
		for (Measurement measurement : measurements) {
			double l0 = measurement.l0;
			double radius = measurement.radius;
			
			results.add(new MeasurementResult(ArrayUtils.findClosest(xSeries, l0 - radius), ArrayUtils.findClosest(xSeries, l0 + radius)));
		}
	}
	
	public MeasurementResult get(int index) {
		return results.get(index);
	}
	
	@Override
	public Iterator<MeasurementResult> iterator() {
		return new Iterator<MeasurementResult>() {
			private int i = 0;
			
			@Override
			public MeasurementResult next() {
				return results.get(i++);
			}
			
			@Override
			public boolean hasNext() {
				return i < results.size();
			}
		};
	}

}
