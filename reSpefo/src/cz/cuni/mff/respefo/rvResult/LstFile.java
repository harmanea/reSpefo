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

class LstFile {
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
			
			int index, exp;
			double julianDate, rvCorr;
			String date, name = null;
			
			int inc; // used if there is a file name between exp and JD
			
			records = new ArrayList<>();
			
			while ((line = br.readLine()) != null) {
				tokens = line.trim().replaceAll(" +", " ").split(" ");
				
				try {
					index = Integer.parseInt(tokens[0]);
					
					date = tokens[1] + ' ' + tokens[2] + ' ' + tokens[3] + ' ' + tokens[4] + ' ' + tokens[5] + ' ' + tokens[6];
					
					exp = Integer.parseInt(tokens[7]);
					
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
					LOGGER.log(Level.FINEST, "Skipped line (" + line + ")", e);
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
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @return the records
	 */
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
