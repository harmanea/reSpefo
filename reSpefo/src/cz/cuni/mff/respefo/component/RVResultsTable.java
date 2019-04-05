package cz.cuni.mff.respefo.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.SpefoException;
import cz.cuni.mff.respefo.util.StringUtils;

public class RVResultsTable {
	private String header;
	private String[] categories;
	private List<RVResultsTableRow> rows;
	
	public RVResultsTable(LstFile lstFile, RVResults[] rvResultsArray) {
		header = lstFile.getHeader();
		categories = Arrays.stream(rvResultsArray).filter(Objects::nonNull).map(r -> r.getCategories())
				.flatMap(Stream::of).distinct().sorted().toArray(String[]::new);
		
		rows = new ArrayList<>();
		
		for (int i = 0; i < lstFile.recordsCount(); i++) {
			LstFileRecord record = lstFile.getAt(i);
			RVResults results = rvResultsArray[i];
			
			Map<String, Double> rvPerCategory = new HashMap<>();
			Map<String, Double> rmsePerCategory = new HashMap<>();
			
			if (results != null) {
				for (String category : categories) {
					double rv = results.getRvOfCategory(category);
					
					if (!Double.isNaN(rv)) {
						if (results.getRvCorr().isUndefined()) {
							rv += record.getRvCorr();
						}
						rvPerCategory.put(category, rv);
						
						if (results.getResultsOfCategory(category).length > 1) {
							rmsePerCategory.put(category, results.getRmseOfCategory(category));
						}
					}
				}
			}
			
			rows.add(new RVResultsTableRow(record, rvPerCategory, rmsePerCategory));
		}
	}
	
	public RVResultsTable(String fileName) throws SpefoException {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			String tokens[];
			
			List<String> headerLines = new ArrayList<>();
			for (int i = 0; i < 4; ++i) {
				line = br.readLine();
				if (!line.isEmpty()) {
					headerLines.add(line);
				}
			}
			header = String.join("\n", headerLines);
			
			tokens = br.readLine().split("\t");
			if (tokens.length < 4) {
				throw new SpefoException("rvs file is invalid.");
			}
			categories = Arrays.copyOfRange(tokens, 3, tokens.length);
			
			rows = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				tokens = line.split("\t");
				
				LstFileRecord record = new LstFileRecord();
				record.setIndex(Integer.parseInt(tokens[0]));
				record.setJulianDate(Double.parseDouble(tokens[1]));
				record.setRvCorr(Double.parseDouble(tokens[2]));
				
				Map<String, Double> rvPerCategory = new HashMap<>();
				for (int i = 0; i < categories.length; i++) {
					String category = categories[i];
					double rv = Double.parseDouble(tokens[i + 3]);
					
					if (rv < 9999.99) {
						rvPerCategory.put(category, rv);
					}	
				}
				
				rows.add(new RVResultsTableRow(record, rvPerCategory, null));
			}
			
		} catch (IOException | NullPointerException | IndexOutOfBoundsException | NumberFormatException exception) {
			throw new SpefoException(exception.getMessage());
		}
	}
	
	public void printToRvsFile(File file) throws SpefoException {
		printToFile(file, false);
	}
	
	public void printToCorFile(File file) throws SpefoException {
		printToFile(file, true);
	}
	
	private void printToFile(File file, boolean correct) throws SpefoException {
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println(header);
			writer.println();
			
			printTableHeader(writer, correct);
			
			for (RVResultsTableRow row : rows) {
				printRow(writer, row, correct);
			}
			
			if (writer.checkError()) {
				throw new IOException("PrintWriter encountered and error.");
			}
		} catch (IOException e) {
			throw new SpefoException(e.getMessage());
		}
	}
	
	private void printTableHeader(PrintWriter writer, boolean correct) {
		writer.print("  N.\tJul. date ");
		if (!correct) {
			writer.print("\t corr ");
		}
		
		for (String category : categories) {
			if (!correct || !category.equals("corr")) {
				writer.print("\t" + StringUtils.paddedString(category, 8));
			}
		}
		
		writer.println();
	}
	
	private void printRow(PrintWriter writer, RVResultsTableRow row, boolean correct) throws SpefoException {
		writer.print(MathUtils.formatDouble(row.getLstFileRecord().getIndex(), 3, 0, false) + "\t");
		writer.print(MathUtils.formatDouble(row.getLstFileRecord().getJulianDate(), 5, 4, false));
		
		double rvCorr = 0;
		if (!correct) {
			writer.print("\t" + MathUtils.formatDouble(row.getLstFileRecord().getRvCorr(), 2, 2, true));
		} else {
			rvCorr = row.getResult("corr").orElseThrow(() -> new SpefoException("Not all rvr files contain measured corrections.")) - row.getLstFileRecord().getRvCorr();
		}
		
		String nextLine = correct ? "\n\t\t" : "\n\t\t\t      ";
		for (String category : categories) {
			if (correct && category.equals("corr")) {
				continue;
			}
			
			Optional<Double> rvOptional = row.getResult(category);
			double rv;
			
			if (rvOptional.isPresent()) {
				rv = rvOptional.get() + rvCorr;
				
			} else {
				rv = 9999.99;
			}
			
			nextLine += "\t" + MathUtils.formatDouble(row.getRmse(category).orElse(0.0), 5, 2, false);
			
			writer.print("\t" + MathUtils.formatDouble(rv, 4, 2, true));
		}
		
		writer.println(nextLine);
	}

	public boolean addTableRow(RVResultsTableRow row) {
		return rows.add(row);
	}
	
	public RVResultsTableRow getTableRow(int index) {
		return rows.get(index);
	}
}
