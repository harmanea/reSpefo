package cz.cuni.mff.respefo.rvResult;

import java.util.Map;
import java.util.Optional;

public class RVResultsTableRow {
	private LstFileRecord lstFileRecord;
	private Map<String, Double> rvPerCategory;
	private Map<String, Double> rmsePerCategory;
	
	public RVResultsTableRow(LstFileRecord lstFileRecord, Map<String, Double> rvPerCategory, Map<String, Double> rmsePerCategory) {
		super();
		this.lstFileRecord = lstFileRecord;
		this.rvPerCategory = rvPerCategory;
		this.rmsePerCategory = rmsePerCategory;
	}

	public LstFileRecord getLstFileRecord() {
		return lstFileRecord;
	}

	public Map<String, Double> getRvPerCategory() {
		return rvPerCategory;
	}
	
	public Map<String, Double> getRmsePerCategory() {
		return rmsePerCategory;
	}
	
	public Optional<Double> getResult(String category) {
		return Optional.ofNullable(rvPerCategory.get(category));
	}

	public Optional<Double> getRmse(String category) {
		return Optional.ofNullable(rmsePerCategory.get(category));
	}
}
