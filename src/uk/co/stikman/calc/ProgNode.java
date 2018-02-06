package uk.co.stikman.calc;

import java.math.BigDecimal;
import java.util.Deque;

public abstract class ProgNode {

	public abstract void execute(Deque<BigDecimal> stack, Context context);

}
