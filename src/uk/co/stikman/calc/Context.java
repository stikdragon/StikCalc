package uk.co.stikman.calc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Context {

	private Map<String, BigDecimal>	vars	= new HashMap<>();
	private int						base	= 10;

	public int getBase() {
		return base;
	}

	public void setVar(String name, BigDecimal value) {
		vars.put(name, value);
	}

	public List<String> getDefinedVariables() {
		return vars.entrySet().stream().map(x -> x.getKey()).collect(Collectors.toList());
	}

	/**
	 * Can return <code>null</code>
	 * 
	 * @param name
	 * @return
	 */
	public BigDecimal getVariable(String name) {
		return vars.get(name);
	}

	public void setBase(int n) {
		this.base = n;
	}
}
