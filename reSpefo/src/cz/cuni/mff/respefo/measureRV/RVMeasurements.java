package cz.cuni.mff.respefo.measureRV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Util;

class RVMeasurements {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	private ArrayList<RVMeasurement> measurements;
	
	public RVMeasurements() {
		measurements = new ArrayList<>();
	}
	
	public void loadMeasurements(String[] fileNames, boolean areCorrections) {
		if (fileNames != null) {
			for (String fileName : fileNames) {
				File file = new File(fileName);
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
					String line;
					String[] tokens;
					
					while ((line = br.readLine()) != null) {
						tokens = line.trim().replaceAll(" +", " ").split(" ", 3);
						if (tokens.length < 3) {
							continue;
						} else {
							try {
								double l0 = Double.valueOf(tokens[0]);
								double radius = Double.valueOf(tokens[1]);
								String name = tokens[2];

								measurements.add(new RVMeasurement(l0, radius, name, areCorrections));
							} catch (Exception e) {
								continue;
							}
						}
					}
				} catch (IOException e) {
					LOGGER.log(Level.WARNING, "Error loading file " + fileName, e);
					MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_ERROR | SWT.OK);
					mb.setText("Error loading file: " + fileName);
					mb.open();
				}
			}
		}
	}
	
	public void removeInvalidMeasurements(double[] xSeries) {
		for (int i = 0; i < measurements.size(); i++) {
			double l0 = measurements.get(i).l0;
			double radius = measurements.get(i).radius;
			if (Util.trimArray(xSeries, l0 - radius, l0 + radius).length == 0) {
				LOGGER.log(Level.INFO, "Measurement " + measurements.get(i).name + " has no valid data points in it's range.");
				
				measurements.remove(i);
				i--;
			}
		}
	}
	
	public int getCount() {
		return measurements.size();
	}
	
	public int getNonCorrectionsCount() {
		int count = 0;
		for (RVMeasurement m : measurements) {
			if (!m.isCorrection) {
				count++;
			}
		}
		
		return count;
	}
	
	public RVMeasurement getAt(int index) {
		if (index >= 0 && index < measurements.size()) {
			return measurements.get(index);
		} else {
			return null;
		}
	}
	
	public void remove(int index) {
		if (index >= 0 && index < measurements.size()) {
			measurements.remove(index);
		}
	}
	
	public String[] getNames() {
		ArrayList<String> names = new ArrayList<>();
		for (RVMeasurement m : measurements) {
			if (m.isCorrection) {
				names.add("* " + m.name);
			} else {
				names.add(m.name);
			}
		}
		
		return names.toArray(new String[names.size()]);
	}
}
