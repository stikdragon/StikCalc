package uk.co.stikman.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Deque;

public class ProgNodeOp extends ProgNode {
	private OpType type;

	public OpType getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public ProgNodeOp(OpType type) {
		super();
		this.type = type;
	}

	@Override
	public String toString() {
		return "Op:     " + type;
	}

	@Override
	public void execute(Deque<BigDecimal> stack, Context context) {
		BigDecimal a;
		BigDecimal b;
		switch (type) {
		case DIVIDE:
			a = stack.pop();
			b = stack.pop();
			int s = Math.max(a.scale(), b.scale());
			stack.push(a.divide(b, s + 100, RoundingMode.HALF_EVEN).stripTrailingZeros());
			break;
		case MINUS:
			a = stack.pop();
			b = stack.pop();
			stack.push(a.subtract(b));
			break;
		case MULTIPLY:
			a = stack.pop();
			b = stack.pop();
			stack.push(a.multiply(b));
			break;
		case PLUS:
			a = stack.pop();
			b = stack.pop();
			stack.push(a.add(b));
			break;
		case UNARY_MINUS:
			a = stack.pop();
			stack.push(a.multiply(new BigDecimal(-1)));
			break;
		case UNARY_PLUS:
			break;
		case POWER:
			a = stack.pop(); // TODO: this is converting to a java double, so losing precision
			b = stack.pop();
			stack.push(new BigDecimal(Math.pow(a.doubleValue(), b.doubleValue())));
			break;

		default:
			throw new RuntimeException("Unknown op: " + type);
		}
	}

	private static final BigDecimal TWO = new BigDecimal("2");

	// from https://stackoverflow.com/questions/13649703/square-root-of-bigdecimal-in-java
	public static BigDecimal sqrt(BigDecimal A, final int SCALE) {
		BigDecimal x0 = BigDecimal.ZERO;
		BigDecimal x1 = new BigDecimal(Math.sqrt(A.doubleValue()));
		while (!x0.equals(x1)) {
			x0 = x1;
			x1 = A.divide(x0, SCALE, RoundingMode.HALF_UP);
			x1 = x1.add(x0);
			x1 = x1.divide(TWO, SCALE, RoundingMode.HALF_UP);
		}
		return x1;
	}

}
