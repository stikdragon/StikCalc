package uk.co.stikman.calc;

public class Token {

	private TokenType	type;
	private int			length;
	private String		value;
	private int			position;

	public Token(TokenType type, int len, String val, int pos) {
		this.type = type;
		this.length = len;
		this.value = val;
		this.position = pos;
	}

	public Token(TokenType type, int len, String val) {
		this(type, len, val, -1);
	}

	public TokenType getType() {
		return type;
	}

	public int getLength() {
		return length;
	}

	public String getValue() {
		return value;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return value + " (" + type.name() + ")";
	}

	public boolean isConstant() {
		return type == TokenType.CONSTANT_FLOAT || type == TokenType.CONSTANT_HEX || type == TokenType.CONSTANT_INT;
	}

	public boolean isOperator() {
		return type != null && type.isOperator();
	}

	public boolean isPreOp() {
		return type == TokenType.PLUS || type == TokenType.MINUS;
	}

}