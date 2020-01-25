package cz.cuni.mff.respefo.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.SpefoException;

public class LstFile {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());

	private String header, fileName;
	private ArrayList<LstFileRecord> records;
	
	public LstFile(String fileName) throws SpefoException {
		this.fileName = fileName;
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			String tokens[];
			
			List<String> headerLines = new ArrayList<>();
			for (int i = 0; i < 4; ++i) {
				headerLines.add(br.readLine());
			}
			header = String.join("\n", headerLines);
			
			while (!(line = br.readLine()).isEmpty()) {
				// skip table header
			}
			
			int index;
			double exp, julianDate, rvCorr;
			String date, name = null;
			
			int offset; // used if there is a file name between exp and JD
			
			records = new ArrayList<>();
			
			while ((line = br.readLine()) != null) {
				tokens = line.trim().replaceAll(" +", " ").split("\\s+");
				
				try {
					index = Integer.parseInt(tokens[0]);
					
					date = tokens[1] + ' ' + tokens[2] + ' ' + tokens[3] + ' ' + tokens[4] + ' ' + tokens[5] + ' ' + tokens[6];
					
					exp = Double.parseDouble(tokens[7]);
					
					if (tokens.length > 10) {
						offset = 1;
						
						name = tokens[8];
					} else {
						offset = 0;
					}
					
					julianDate = Double.parseDouble(tokens[8 + offset]);
					
					rvCorr = Double.parseDouble(tokens[9 + offset]);
					if (offset == 0) {
						records.add(new LstFileRecord(index, exp, julianDate, rvCorr, date));
					} else {
						records.add(new LstFileRecord(index, exp, julianDate, rvCorr, date, name));
					}
					
				} catch (NumberFormatException | IndexOutOfBoundsException e) {
					LOGGER.log(Level.WARNING, "Skipped line (" + line + ")", e);
					continue;
				}
			}

		} catch (IOException | NullPointerException e) {
			throw new SpefoException(e.getMessage());
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
	
	public void save() throws SpefoException {
		File file = new File(fileName);
		try (PrintWriter wr = new PrintWriter(new FileOutputStream(file))) {
			wr.println(header);
			wr.println(getTableHeader());
			
			for (LstFileRecord record : records) {
				wr.println(String.join(" ", 
								MathUtils.formatInteger(record.getIndex(), 5),
								record.getDate(),
								MathUtils.formatDouble(record.getExp(), 5, 3, false),
								record.getFileName() != null ? Paths.get(record.getFileName()).getFileName().toString() : "        ",
								MathUtils.formatDouble(record.getJulianDate(), 5, 4),
								MathUtils.formatDouble(record.getRvCorr(), 3, 2)));
			}
			
		} catch (FileNotFoundException exception) {
			throw new SpefoException(exception.getMessage());
		}
	}
	
	public String getTableHeader() { // TODO change this
		if (records.stream().anyMatch(record -> record.hasFileName())) {
			return "==============================================================================\n" + 
					"   N.  Date & UT start       exp[s]      Filename       J.D.hel.  RVcorr\n" + 
					"==============================================================================\n";
		} else {
			return "==============================================================================\n" + 
					"   N.  Date & UT start       exp[s]                     J.D.hel.  RVcorr\n" + 
					"==============================================================================\n";
		}
	}
}
