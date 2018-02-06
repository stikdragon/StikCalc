package uk.co.stikman.calc;

public enum TokenType {
	WHITESPACE(false), EOF(false), BOF(false), UNKNOWN(false),

	COLON(false),

	CONSTANT_HEX(false), CONSTANT_FLOAT(false), CONSTANT_INT(false),

	IDENTIFIER(false), PERIOD(true), COMMA(false), OPEN_PAREN(true), CLOSE_PAREN(false), ASSIGNMENT(false), PLUS(true), MINUS(true), DIVIDE(true), MULTIPLY(true), CARET(true);

	private boolean operator;

	private TokenType(boolean operator) {
		this.operator = operator;
	}

	public boolean isOperator() {
		return operator;
	}

}