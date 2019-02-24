package cz.cuni.mff.respefo.measureRV;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.SpefoException;

public class RVResults {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	private ArrayList<RVResult> results;
	private double helCorr;
	
	public RVResults() {
		this(Double.NaN);
	}
	
	public RVResults(double helCorr) {
		results = new ArrayList<>();
		
		this.helCorr = helCorr;
	}
	
	public RVResults(String rvrFileName) throws SpefoException {
		results = new ArrayList<>();
		
		try (BufferedReader br = Files.newBufferedReader(Paths.get(rvrFileName))) {
			String line;
			String[] tokens;
			
			// skip header
			br.readLine();
			br.readLine();
			br.readLine();
			
			line = br.readLine();
			if (line == null) {
				throw new SpefoException(".rvr file is invalid.");
			}
			Matcher matcher = Pattern.compile("(Heliocentric|Barycentric)( correction:)(.*)").matcher(line);
			if (!matcher.matches() || matcher.groupCount() != 3) {
				throw new SpefoException(".rvr file is invalid.");
			}
			try {
				helCorr = Double.parseDouble(matcher.group(3));
			} catch (NumberFormatException numberFormatException) {
				helCorr = Double.NaN;
			}
			
			line = br.readLine(); // skip blank line
			
			Pattern pattern = Pattern.compile("(## Results for category )(.*)(:)");
			while ((line = br.readLine()) != null) {
				matcher = pattern.matcher(line);
				if (!matcher.matches() || matcher.groupCount() != 3) {
					throw new SpefoException(".rvr file is invalid.");
				}
				String category = matcher.group(2);
				
				line = br.readLine(); // skip table header
				while ((line = br.readLine()) != null) {
					if (line.isEmpty()) {
						break;
					}
					
					tokens = line.split("\t", -1);
					if (tokens.length != 5) {
						continue;
					}
					
					double rv = Double.parseDouble(tokens[0]);
					double radius = Double.parseDouble(tokens[1]);
					double lambda = Double.parseDouble(tokens[2]);
					String name = tokens[3];
					String comment = tokens[4];
					
					RVResult result = new RVResult(rv, Double.NaN, radius, category, lambda, name, comment, 0);
					results.add(result);
				}
			}
			
		} catch (SpefoException spefoException) {
			throw spefoException;
		} catch (Exception exception) {
			throw new SpefoException(exception.getMessage());
		}
	}
	
	public void addResult(RVResult result) {
		results.add(result);
	}

	public double getHelCorr() {
		return helCorr;
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
		return results.stream()
				.map(result -> result.category).distinct().toArray(String[]::new);
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
	
	public double getRvOfCategory(String category) {
		RVResult[] results = getResultsOfCategory(category);
		
		return Arrays.stream(results).mapToDouble(result -> result.rV).average().orElse(Double.NaN);
	}
}
