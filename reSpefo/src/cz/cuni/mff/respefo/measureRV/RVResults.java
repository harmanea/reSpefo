package cz.cuni.mff.respefo.measureRV;

import java.util.ArrayList;
import java.util.logging.Logger;

import cz.cuni.mff.respefo.ReSpefo;

class RVResults {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	private ArrayList<RVResult> results;
	
	public RVResults() {
		results = new ArrayList<>();
	}
	
	public void addResult(RVResult result) {
		results.add(result);
	}
	
	public int getCount() {
		return results.size();
	}
	
	public RVResult getAt(int index) {
		if (index >= 0 && index < results.size()) {
			return results.get(index);
		} else {
			return null;
		}
	}
	
	public void remove(int index) {
		if (index >= 0 && index < results.size()) {
			results.remove(index);
		}
	}
	
	public String[] getCategories() {
		ArrayList<String> categories = new ArrayList<>();
		for (RVResult result : results) {
			if (!categories.contains(result.category)) {
				categories.add(result.category);
			}
		}
		
		categories.sort(String::compareTo);
		return categories.toArray(new String[categories.size()]);
	}
	
	public RVResult[] getResultsOfCategory(String category) {
		ArrayList<RVResult> matchingResults = new ArrayList<>();
		for (RVResult result : results) {
			if (result.category.equals(category)) {
				matchingResults.add(result);
			}
		}
		
		return matchingResults.toArray(new RVResult[matchingResults.size()]);
	}
	
	public void printToFile(String fileName) {
		// TODO implement this
	}
}
