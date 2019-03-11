package cz.cuni.mff.respefo.preparation;

public class LstRecordDto {
	private int index, exp;
	private double julianDate, rvCorr;
	private String date, fileName;
	
	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	/**
	 * @return the exp
	 */
	public int getExp() {
		return exp;
	}
	/**
	 * @param exp the exp to set
	 */
	public void setExp(int exp) {
		this.exp = exp;
	}
	/**
	 * @return the julianDate
	 */
	public double getJulianDate() {
		return julianDate;
	}
	/**
	 * @param julianDate the julianDate to set
	 */
	public void setJulianDate(double julianDate) {
		this.julianDate = julianDate;
	}
	/**
	 * @return the rvCorr
	 */
	public double getRvCorr() {
		return rvCorr;
	}
	/**
	 * @param rvCorr the rvCorr to set
	 */
	public void setRvCorr(double rvCorr) {
		this.rvCorr = rvCorr;
	}
	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public String toString() {
		return "LstRecordDto [index=" + index + ", exp=" + exp + ", julianDate=" + julianDate + ", rvCorr=" + rvCorr
				+ ", date=" + date + ", fileName=" + fileName + "]";
	}
}
