package cz.cuni.mff.respefo.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.MathUtils;

public class RectifyPoints {
	private List<Double> xCoordinates;
	private List<Double> yCoordinates;
	
	private int activeIndex;
	
	public RectifyPoints() {
		xCoordinates = new ArrayList<>();
		yCoordinates = new ArrayList<>();
		
		activeIndex = 0;
	}
	
	public int getCount() {
		return xCoordinates.size();
	}
	
	public double getXAt(int index) {
		if (index >= 0 && index < xCoordinates.size()) {
			return xCoordinates.get(index);
		} else {
			return Double.NaN;
		}
	}
	
	public double getYAt(int index) {
		if (index >= 0 && index < yCoordinates.size()) {
			return yCoordinates.get(index);
		} else {
			return Double.NaN;
		}
	}
	
	public double getActiveX() {
		return xCoordinates.get(activeIndex);
	}
	
	public double getActiveY() {
		return yCoordinates.get(activeIndex);
	}
	
	public void changeActivePoint(int indexChange) {
		setActivePoint(activeIndex + indexChange);
	}
	
	public void setActivePoint(int index) {
		activeIndex = index;
		if (activeIndex < 0) {
			activeIndex = 0;
		} else if (activeIndex >= xCoordinates.size()) {
			activeIndex = xCoordinates.size() - 1;
		}
	}
	
	public int getActiveIndex() {
		return activeIndex;
	}
	
	public double[] getXCoordinates() { 
		return xCoordinates.stream().mapToDouble(Double::doubleValue).toArray();
	}
	
	public double[] getYCoordinates() {
		return yCoordinates.stream().mapToDouble(Double::doubleValue).toArray();
	}
	
	public double[] getIntepData(double[] xinter) {
		return MathUtils.intep(getXCoordinates(), getYCoordinates(), xinter);
	}
	
	public void addPoint(double x, double y) {
		int index = ArrayUtils.findFirstGreaterThen(getXCoordinates(), x);
		
		xCoordinates.add(index, x);
		yCoordinates.add(index, y);
		
		activeIndex = index;
	}
	
	public void removePoint(int index) {
		if (getCount() > 1 && index >= 0 && index < xCoordinates.size()) {
			xCoordinates.remove(index);
			yCoordinates.remove(index);
			
			if (activeIndex == index && index > 0) {
				activeIndex--;
			}
		}
	}
	
	public void movePoint(double xShift, double yShift, int index) {
		if (index >= 0 && index < xCoordinates.size()) {
			double oldX = xCoordinates.get(index);
			double oldY = yCoordinates.get(index);
			
			removePoint(index);
			addPoint(oldX + xShift, oldY + yShift); 
		}
	}
	
	public int findClosest(double x, double y) {
		int index = -1;
		double closest = Double.MAX_VALUE;
		for (int i = 0; i < xCoordinates.size(); i++) {
			double distance = Math.pow(x - xCoordinates.get(i), 2) + Math.pow(y - yCoordinates.get(i), 2);
			if (distance < closest) {
				index = i;
				closest = distance;
			}
		}
		
		return index;
	}
	
	public void reset(double x1, double y1, double x2, double y2) {
		xCoordinates.clear();
		yCoordinates.clear();
		
		activeIndex = 0;
		
		xCoordinates.add(x1);
		xCoordinates.add(x2);
		
		yCoordinates.add(y1);
		yCoordinates.add(y2);
	}
	
	public void adjustToNewData(double[] xSeries, double[] ySeries) {
		xCoordinates = xCoordinates.stream().filter(x -> x >= xSeries[0] && x <= xSeries[xSeries.length - 1]).collect(Collectors.toList());
		
		yCoordinates.clear();
		xCoordinates.forEach(x -> {
			yCoordinates.add(
					findClosest(xSeries, x).stream()
						.mapToDouble(index -> ySeries[index])
						.average()
						.getAsDouble());
		});
	}
	
	private List<Integer> findClosest(double[] series, double value) {
		Comparator<Integer> distanceToValueComparator = new Comparator<Integer>() {
			
			@Override
			public int compare(Integer a, Integer b) {
				double aDistance = Math.abs(series[a] - value);
				double bDistance = Math.abs(series[b] - value);
				
				return (int) (aDistance - bDistance);
			}
		};
		
		PriorityQueue<Integer> distanceToValueQueue = new PriorityQueue<>(distanceToValueComparator);
		for (int i = 0; i < series.length; i++) {
			distanceToValueQueue.add(i);
		}
		
		if (distanceToValueQueue.isEmpty()) {
			return Collections.singletonList(0);
		}
		
		List<Integer> result = new ArrayList<>(5);
		for (int i = 0; i < 5 && !distanceToValueQueue.isEmpty(); i++) {
		
			result.add(distanceToValueQueue.remove());
		}
		
		return result;
	}
}
