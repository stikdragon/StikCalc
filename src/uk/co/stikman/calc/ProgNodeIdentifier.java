package uk.co.stikman.calc;

import java.math.BigDecimal;
import java.util.Deque;

public class ProgNodeIdentifier extends ProgNode {
	private String name;

	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public ProgNodeIdentifier(String name) {
		super();
		this.name = name;
	}

	@Override
	public String toString() {
		return "Symbol: " + name;
	}

	@Override
	public void execute(Deque<BigDecimal> stack, Context context) {
		BigDecimal v = context.getVariable(getName());
		if (v == null)
			throw new EvaluationException("Variable " + getName() + " has not been assigned");
		stack.push(v);
	}

}
