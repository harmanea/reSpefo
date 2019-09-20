package cz.cuni.mff.respefo.component;

public class LstFileRecord implements Comparable<LstFileRecord> { 
	public LstFileRecord() {
		
	}
	
	public LstFileRecord(int index, double exp, double julianDate, double rvCorr, String date) {
		this.index = index;
		this.exp = exp;
		this.julianDate = julianDate;
		this.rvCorr = rvCorr;
		this.date = date;
	}
	
	public LstFileRecord(int index, double exp, double julianDate, double rvCorr, String date, String fileName) {
		this.index = index;
		this.exp = exp;
		this.julianDate = julianDate;
		this.rvCorr = rvCorr;
		this.date = date;
		this.fileName = fileName;
	}

	public int getIndex() {
		return index;
	}

	public double getExp() {
		return exp;
	}

	public double getJulianDate() {
		return julianDate;
	}
	
	public double getRvCorr() {
		return rvCorr;
	}
	
	/**
	 * @return a formatted version of the date
	 */
	public String getFormattedDate() {
		String[] tokens = date.split(" ");
		
		return tokens[2] + ". " + tokens[1] + ". " + tokens[0];
	}

	public String getDate() {
		return date;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public boolean hasFileName() {
		return fileName != null;
	}
 	
	@Override
	public String toString() {
		return Integer.toString(index) + ' ' + date + ' ' + Double.toString(exp) + ' ' + Double.toString(julianDate)
				+ ' ' + Double.toString(rvCorr) + ((fileName != null) ? (' ' + fileName) : "");
	}
	
	@Override
	public int compareTo(LstFileRecord other) {
		if (this.date == null || other.date == null) {
			return Double.compare(this.julianDate, other.julianDate);
		} else if (this.date.equals(other.date) && this.fileName != null && other.fileName != null) {
			return this.fileName.compareToIgnoreCase(other.fileName);
		} else {
			return this.date.compareTo(other.date);
		}
	}

	private int index;
	private double exp, julianDate, rvCorr;
	private String date, fileName;
	
	public void setIndex(int index) {
		this.index = index;
	}

	public void setExp(double exp) {
		this.exp = exp;
	}

	public void setJulianDate(double julianDate) {
		this.julianDate = julianDate;
	}

	public void setRvCorr(double rvCorr) {
		this.rvCorr = rvCorr;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
