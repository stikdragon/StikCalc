package uk.co.stikman.calc;

public class EvaluationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1166095678212799701L;

	/**
	 * @param arg0
	 * @param arg1
	 */
	public EvaluationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public EvaluationException(String arg0) {
		super(arg0);
	}

}
