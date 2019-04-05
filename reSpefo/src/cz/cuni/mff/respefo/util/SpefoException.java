package cz.cuni.mff.respefo.util;

/**
 * A generic exception
 */
public class SpefoException extends Exception {

	/**
	 * generated serial version UID
	 */
	private static final long serialVersionUID = -2318639174726198978L;

	public SpefoException() {}

	public SpefoException(String message) {
		super(message);
	}

	public SpefoException(Throwable cause) {
		super(cause);
	}

	public SpefoException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpefoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
