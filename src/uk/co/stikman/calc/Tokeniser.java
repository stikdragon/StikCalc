package uk.co.stikman.calc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parts of this are inspired by AS
 * 
 * @author frenchd
 * 
 */
public class Tokeniser {

	private String					source;
	private int						sourcePos;
	private int						sourceLen;
	private Set<String>				reservedKeywords;
	private Map<String, TokenType>	keywordMap;
	private int						maxKeywordLen	= -1;
	private static final char[]		WHITESPACE		= { ' ', '\t', '\r', '\n' };

	public Tokeniser(String src) {
		this.source = src;
		sourcePos = 0;
		sourceLen = src.length();

		keywordMap = new HashMap<>();

		keywordMap.put("(", TokenType.OPEN_PAREN);
		keywordMap.put(")", TokenType.CLOSE_PAREN);
		keywordMap.put(",", TokenType.COMMA);
		keywordMap.put(".", TokenType.PERIOD);
		keywordMap.put("=", TokenType.ASSIGNMENT);
		keywordMap.put("+", TokenType.PLUS);
		keywordMap.put("-", TokenType.MINUS);
		keywordMap.put("*", TokenType.MULTIPLY);
		keywordMap.put("/", TokenType.DIVIDE);
		keywordMap.put(":", TokenType.COLON);
		keywordMap.put("^", TokenType.CARET);

		reservedKeywords = new HashSet<>(keywordMap.keySet());
	}

	private void calculateMaxKeywordLength() {
		for (String s : keywordMap.keySet())
			if (s.length() > maxKeywordLen)
				maxKeywordLen = s.length();
	}

	public Token getToken() {
		Token res;

		int remain = sourceLen - sourcePos;

		if (sourcePos >= sourceLen)
			return new Token(TokenType.EOF, 0, null);

		res = getWhiteSpace(sourcePos, remain);
		if (res != null) {
			sourcePos += res.getLength();
			return res;
		}

		res = getConstant(sourcePos, remain);
		if (res != null) {
			sourcePos += res.getLength();
			return res;
		}

		res = getIdentifier(sourcePos, remain);
		if (res != null) {
			sourcePos += res.getLength();
			return res;
		}

		res = getKeyword(sourcePos, remain);
		if (res != null) {
			sourcePos += res.getLength();
			return res;
		}

		res = new Token(TokenType.UNKNOWN, 1, source.substring(sourcePos, sourcePos + 1));
		++sourcePos;
		return res;
	}

	private Token getKeyword(int offset, int length) {
		if (maxKeywordLen == -1)
			calculateMaxKeywordLength();
		int maxLength = maxKeywordLen;
		if (maxLength >= length)
			maxLength = length;

		//
		// Special case for lists, speeds things up a bit
		//
		if (source.charAt(offset) == ',')
			return new Token(TokenType.COMMA, 1, source.substring(offset, offset + 1));

		//
		// Look for things in the keyword list, start frmo the longest and work backwards
		//
		while (maxLength > 0) {
			TokenType tt = keywordMap.get(source.substring(offset, offset + maxLength));
			if (tt != null) {
				if (maxLength < length) {
					char ch = source.charAt(offset + maxLength);
					char chleft = source.charAt(offset + maxLength - 1);
					if (maxLength < sourceLen && (isAlpha(chleft)) && (isAlphaNumUS(ch))) {
						//
						// Isn't really a match since it's followed by a coincidental bit of identifier
						//
						--maxLength;
						continue;
					}
				}
				return new Token(tt, maxLength, source.substring(offset, offset + maxLength));
			}
			--maxLength;
		}
		return null;
	}

	private Token getIdentifier(int offset, int length) {
		//
		// Identifiers start with [a-z_]
		//
		if (isAlphaUS(source.charAt(offset))) {

			TokenType tokenType = TokenType.IDENTIFIER;
			int tokenLength = 1;

			for (int n = 1; n < length; ++n) {
				if (isAlphaNumUS(source.charAt(offset + n)))
					tokenLength++;
				else
					break;
			}

			//
			// Make sure the identifier isn't a reserved keyword
			//
			if (reservedKeywords.contains(source.subSequence(offset, offset + tokenLength)))
				return null;

			return new Token(tokenType, tokenLength, source.substring(offset, offset + tokenLength));
		}
		return null;
	}

	private Token getConstant(int offset, int length) {
		char a = source.charAt(offset);
		char b = 0;
		if (length > 1)
			b = source.charAt(offset + 1);

		//
		// Starts with number
		//
		if (isNum(a) || (a == '.' && length > 1 && isNum(b))) {

			//
			// Is hex?
			//
			if (a == '0' && length > 1 && (b == 'x' || b == 'X')) {
				int n;
				for (n = 2; n < length; ++n) {
					if (!isHex(source.charAt(offset + n)))
						break;
				}
				return new Token(TokenType.CONSTANT_HEX, n, source.substring(offset, offset + n));
			}

			//
			// Not hex
			//
			int n;
			for (n = 0; n < length; ++n)
				if (!isNum(source.charAt(offset + n)))
					break;

			//
			// Check floating point
			//
			if (n < length && source.charAt(offset + n) == '.') {
				++n;
				for (; n < length; ++n) {
					if (!isNum(source.charAt(offset + n)))
						break;
				}

				if (n < length && (source.charAt(offset + n) == 'e' || source.charAt(offset + n) == 'E')) {
					++n;
					if (n < length && (source.charAt(offset + n) == '-' || source.charAt(offset + n) == '+'))
						++n;

					for (; n < length; ++n) {
						if (!isNum(source.charAt(offset + n)))
							break;
					}
				}

				return new Token(TokenType.CONSTANT_FLOAT, n, source.substring(offset, offset + n));
			}

			//
			// Wasn't float, so must be an int
			//
			return new Token(TokenType.CONSTANT_INT, n, source.substring(offset, offset + n));
		}

		return null;
	}

	private Token getWhiteSpace(int offset, int length) {
		int numWsChars = WHITESPACE.length;
		int n;
		for (n = 0; n < length; ++n) {
			boolean b = false;
			char ch = source.charAt(offset + n);
			for (int w = 0; w < numWsChars; ++w) {
				if (ch == WHITESPACE[w]) {
					b = true;
					break;
				}
			}
			if (!b)
				break;
		}

		if (n > 0) {
			Token res = new Token(TokenType.WHITESPACE, n, source.substring(offset, offset + n));
			return res;
		}

		return null;
	}

	/**
	 * Alphanumeric or underscore
	 * 
	 * @param c
	 * @return
	 */
	private final boolean isAlphaNumUS(final char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c == '_');
	}

	/**
	 * Alpha or underscore
	 * 
	 * @param charAt
	 * @return
	 */
	private final boolean isAlphaUS(final char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c == '_');
	}

	/**
	 * Is alphabetic
	 * 
	 * @param c
	 * @return
	 */
	private final boolean isAlpha(final char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}

	/**
	 * Is in [0-9]
	 * 
	 * @param c
	 * @return
	 */
	private final boolean isNum(final char c) {
		return (c >= '0' && c <= '9');
	}

	private boolean isHex(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

}