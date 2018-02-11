package uk.co.stikman.calc;

import java.math.BigDecimal;
import java.util.Deque;

public class ProgNodeConstant extends ProgNode {
	private BigDecimal value;

	public ProgNodeConstant(BigDecimal val) {
		this.value = val;
	}

	public BigDecimal getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Const:  " + value;
	}

	@Override
	public void execute(Deque<BigDecimal> stack, Context context) {
		stack.push(value);
	}

}
