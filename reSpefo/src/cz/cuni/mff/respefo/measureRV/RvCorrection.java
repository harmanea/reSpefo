package cz.cuni.mff.respefo.measureRV;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.cuni.mff.respefo.SpefoException;

public class RvCorrection {
	public static final int HELIOCENTRIC = 1;
	public static final int BARYCENTRIC = 2;
	public static final int UNDEFINED = -1;
	
	private int type;
	private double value;
	
	public int getType() {
		return type;
	}
	
	public double getValue() {
		return value;
	}

	public RvCorrection(int type, double value) {
		super();
		this.type = type;
		this.value = value;
	}
	
	public boolean isUndefined() {
		return type == UNDEFINED;
	}
	
	public RvCorrection(String rvResultLine) throws SpefoException {
		Matcher matcher = Pattern.compile("(Heliocentric|Barycentric)( correction:)(.*)").matcher(rvResultLine);
		try {
			if (!matcher.matches() || matcher.groupCount() != 3) {
				throw new SpefoException();
			}
			
			this.value = Double.parseDouble(matcher.group(3));
			this.type = matcher.group(1).equals("Heliocentric") ? HELIOCENTRIC : BARYCENTRIC; 
			
		} catch (NumberFormatException | SpefoException exception) {
			
			this.type = UNDEFINED;
			this.value = Double.NaN;
		}
	}
	
	public String toRvResultLine() throws SpefoException {
		
		switch(type) {
		case HELIOCENTRIC:
			return "Heliocentric correction: " + value;
		case BARYCENTRIC:
			return "Barycentric correction: " + value;
		case UNDEFINED:
			return "RV correction not defined.";
		default:
			throw new SpefoException("Invalid RvCorrection type");
		}
	}
}
