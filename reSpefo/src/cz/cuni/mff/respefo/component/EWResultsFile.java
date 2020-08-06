package cz.cuni.mff.respefo.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.cuni.mff.respefo.util.SpefoException;

public class EWResultsFile {
	private Map<String, Map<String, Double>> values;
	private boolean valid;
	
	private EWResultsFile() {
		valid = false;
	}
	
	public static EWResultsFile invalidFile() {
		return new EWResultsFile();
	}
	
	public EWResultsFile(String fileName) throws SpefoException {
		values = new HashMap<>();
		valid = true;
		
		try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {
			
			// skip header
			br.readLine();
			br.readLine();
			br.readLine();
			
			String line;
			Pattern pattern = Pattern.compile("(\\s*)(\\S*)(:\\s*)([\\d-\\.]*)(.*)");
			while ((line = br.readLine()) != null) {
				String name = line;
				Map<String, Double> map = new HashMap<>();
				
				while ((line = br.readLine()) != null) {
					if (line.isEmpty()) {
						break;
					}
					
					Matcher matcher = pattern.matcher(line);
					if (!matcher.matches() || matcher.groupCount() != 5) {
						throw new SpefoException(".eqw file is invalid.");
					}
					
					String category = matcher.group(2);
					String number = matcher.group(4);
					double value = Double.parseDouble(number);
					
					map.put(category, value);
				}
				
				values.put(name, map);
			}			
			
		} catch (IOException | NumberFormatException exception) {
			throw new SpefoException(exception);
		}
	}
	
	public Set<String> getNames() {
		return values.keySet();
	}
	
	public Map<String, Double> forName(String name) {
		return values.get(name);
	}
	
	public boolean isValid() {
		return valid;
	}
}
