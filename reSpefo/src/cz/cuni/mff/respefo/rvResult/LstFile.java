package cz.cuni.mff.respefo.rvResult;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.SpefoException;

public class LstFile {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());

	private String header, info, fileName;
	private ArrayList<LstFileRecord> records;
	
	public LstFile(String fileName) throws SpefoException {
		this.fileName = fileName;
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			String tokens[];
			
			header = br.readLine();
			
			List<String> infoLines = new ArrayList<>();
			while (!(line = br.readLine()).isEmpty()) {
				infoLines.add(line);
			}
			info = String.join("\n", infoLines);
			
			while (!(line = br.readLine()).isEmpty()) {
				// skip table header
			}
			
			int index;
			double exp, julianDate, rvCorr;
			String date, name = null;
			
			int inc; // used if there is a file name between exp and JD
			
			records = new ArrayList<>();
			
			while ((line = br.readLine()) != null) {
				tokens = line.trim().replaceAll(" +", " ").split(" ");
				
				try {
					index = Integer.parseInt(tokens[0]);
					
					date = tokens[1] + ' ' + tokens[2] + ' ' + tokens[3] + ' ' + tokens[4] + ' ' + tokens[5] + ' ' + tokens[6];
					
					exp = Double.parseDouble(tokens[7]);
					
					if (tokens.length > 10) {
						inc = 1;
						
						name = tokens[8];
					} else {
						inc = 0;
					}
					
					julianDate = Double.parseDouble(tokens[8 + inc]);
					
					rvCorr = Double.parseDouble(tokens[9 + inc]);
					if (inc == 0) {
						records.add(new LstFileRecord(index, exp, julianDate, rvCorr, date));
					} else {
						records.add(new LstFileRecord(index, exp, julianDate, rvCorr, date, name));
					}
					
				} catch (NumberFormatException | IndexOutOfBoundsException e) {
					LOGGER.log(Level.WARNING, "Skipped line (" + line + ")", e);
					continue;
				}
			}
			
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Error while reading file", e);
			// TODO specialized exception?
			throw new SpefoException("IOException occurred!");
		} catch (NullPointerException e) {
			LOGGER.log(Level.WARNING, "File has invalid format", e);
			// TODO specialized exception?
			throw new SpefoException("NullPointerException occurred!");
		}
		
	}
	
	public LstFile() {
		records = new ArrayList<>();
	}

	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getHeader() {
		return header;
	}
	
	public void setHeader(String header) {
		this.header = header;
	}

	public String getInfo() {
		return info;
	}

	public void addRecord(LstFileRecord record) {
		records.add(record);
	}
	
	public void addRecords(List<LstFileRecord> records) {
		this.records.addAll(records);
	}
	
	public ArrayList<LstFileRecord> getRecords() {
		return records;
	}
	
	public LstFileRecord getAt(int index) {
		return records.get(index);
	}
	
	public int recordsCount() {
		return records.size();
	}
}
