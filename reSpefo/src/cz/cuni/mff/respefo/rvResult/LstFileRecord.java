package cz.cuni.mff.respefo.rvResult;

class LstFileRecord {
	public LstFileRecord(int index, int exp, double julianDate, double rvCorr, String date) {
		this.index = index;
		this.exp = exp;
		this.julianDate = julianDate;
		this.rvCorr = rvCorr;
		this.date = date;
		
		System.out.println(this);
	}
	
	public LstFileRecord(int index, int exp, double julianDate, double rvCorr, String date, String fileName) {
		this.index = index;
		this.exp = exp;
		this.julianDate = julianDate;
		this.rvCorr = rvCorr;
		this.date = date;
		this.fileName = fileName;
		
		System.out.println(this);
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the exp
	 */
	public int getExp() {
		return exp;
	}

	/**
	 * @return the julianDate
	 */
	public double getJulianDate() {
		return julianDate;
	}

	/**
	 * @return the rvCorr
	 */
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
	
	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	
	public boolean hasFileName() {
		return fileName != null;
	}
 	
	@Override
	public String toString() {
		return Integer.toString(index) + ' ' + date + ' ' + Integer.toString(exp) + ' ' + Double.toString(julianDate)
				+ ' ' + Double.toString(rvCorr) + ((fileName != null) ? (' ' + fileName) : "");
	}

	private int index, exp;
	private double julianDate, rvCorr;
	private String date, fileName;
}
