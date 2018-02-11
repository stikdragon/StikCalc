package uk.co.stikman.calc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import uk.co.stikman.calc.Iter.Mark;

public class Calculation {

	private List<ProgNode>	program		= new ArrayList<>();
	private String			resultVar	= null;

	public String getResultVar() {
		return resultVar;
	}

	public List<ProgNode> getProgram() {
		return program;
	}

	public void parse(List<Token> tokens) throws ParseException {

		Iter<Token> iter = new Iter<>(tokens);

		Mark mk = iter.mark();
		if (iter.peekNext().getType() == TokenType.IDENTIFIER) {
			//
			// Looking at "a = <expression>" maybe
			//
			Token name = iter.next();
			if (iter.hasNext() && iter.next().getType() == TokenType.ASSIGNMENT)
				resultVar = name.getValue();
			else // nope
				mk.restore();
		}

		while (iter.hasNext())
			parseExpression(iter);
	}

	private void parseExpression(Iter<Token> iter) throws ParseException {
		Deque<OpType> opstk = new LinkedList<>();

		while (iter.hasNext()) {
			parseOperand(iter);

			//
			// Now looking for an operator
			//
			if (!iter.hasNext())
				break;

			Mark mk = iter.mark();
			Token t = iter.next();
			OpType optype = null;
			boolean breakout = false;
			switch (t.getType()) {
			case EOF:
			case CLOSE_PAREN:
				mk.restore();
				breakout = true;
				break;

			case CARET:
				optype = OpType.POWER;
				break;

			case MINUS:
				optype = OpType.MINUS;
				break;
			case MULTIPLY:
				optype = OpType.MULTIPLY;
				break;
			case DIVIDE:
				optype = OpType.DIVIDE;
				break;
			case PLUS:
				optype = OpType.PLUS;
				break;
			default:
				throw new ParseException("Unexpected operator: " + t);
			}

			if (breakout)
				break;

			while (!opstk.isEmpty()) {
				OpType o2 = opstk.peek();
				if (comparePrecedence(optype, o2) <= 0) {
					opstk.pop();
					program.add(new ProgNodeOp(o2));
				} else
					break;
			}
			opstk.push(optype);
		}

		while (!opstk.isEmpty())
			program.add(new ProgNodeOp(opstk.pop()));
	}

	private static int comparePrecedence(OpType o1, OpType o2) {
		int i1 = getPrecedence(o1);
		int i2 = getPrecedence(o2);
		return i1 - i2;
	}

	/**
	 * Returns an integer to represent the precedence of the operator, can be 0
	 * or negative
	 * 
	 * @param n
	 * @return
	 */
	private static int getPrecedence(OpType n) {
		switch (n) {
		case DIVIDE:
		case MULTIPLY:
			return 10;
		case MINUS:
		case PLUS:
			return 5;
		case UNARY_MINUS:
		case UNARY_PLUS:
			return 15;

		case POWER:
			return 13;
		}
		throw new RuntimeException("Unknown operator: " + n);
	}

	private void parseOperand(Iter<Token> iter) throws ParseException {
		//
		// expecting an identifier, constant, or bracket, or a pre-op
		//
		Token t = iter.next();
		switch (t.getType()) {
		case CONSTANT_FLOAT:
		case CONSTANT_INT:
			program.add(new ProgNodeConstant(new BigDecimal(t.getValue())));
			break;
		case CONSTANT_HEX:
			program.add(new ProgNodeConstant(new BigDecimal(new BigInteger(t.getValue().substring(2), 16))));
			break;

		case OPEN_PAREN:
			parseExpression(iter);
			expect(iter, TokenType.CLOSE_PAREN);
			break;

		case IDENTIFIER:
			program.add(new ProgNodeIdentifier(t.getValue()));
			break;

		case PLUS:
			parseOperand(iter);
			program.add(new ProgNodeOp(OpType.UNARY_PLUS));
			break;

		case MINUS:
			parseOperand(iter);
			program.add(new ProgNodeOp(OpType.UNARY_MINUS));
			break;

		default:
			throw new ParseException("Unexpected token: " + t);
		}
	}

	private void expect(Iter<Token> iter, TokenType type) throws ParseException {
		Token t = iter.peekNext();
		if (t.getType() != type)
			throw new ParseException("Expected a " + type + " but found a " + t.getType());
		iter.next();
	}

	public BigDecimal evaluate(Context context) {
		Deque<BigDecimal> stack = new LinkedList<>();
		for (ProgNode n : program)
			n.execute(stack, context);
		if (stack.size() != 1)
			throw new RuntimeException("Stack did not have exactly one element remaining");
		return stack.getFirst();
	}

}
