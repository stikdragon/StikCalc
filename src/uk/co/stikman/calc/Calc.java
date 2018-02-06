package uk.co.stikman.calc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Calc {

	private static Context		context;
	private static boolean		terminated	= false;
	private static Calculation	lastCalculation;

	public static void main(String[] args) {
		out("Calculator.  Type an expression to evaluate.");
		out("Type :q to quit, and :help for help.");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			context = new Context();
			while (!terminated) {
				String input = br.readLine();
				process(input);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void process(String line) {
		if (line.trim().isEmpty() && lastCalculation != null) {
			runCalculation(lastCalculation);
			return;
		}

		Tokeniser tksr = new Tokeniser(line);
		List<Token> tokens = new ArrayList<>();
		while (true) {
			Token tok = tksr.getToken();
			if (tok.getType() == TokenType.EOF)
				break;
			if (tok.getType() == TokenType.WHITESPACE)
				continue; // ignore whitespace
			tokens.add(tok);
		}

		//		tokens.forEach(t -> out("  " + t));

		if (tokens.isEmpty())
			return;

		Token t = tokens.get(0);
		if (t.getType() == TokenType.COLON) {
			//
			// command mode
			//
			doCommand(tokens.subList(1, tokens.size()));
		} else {
			//
			// calculate mode
			//
			Calculation calc = new Calculation();
			try {
				calc.parse(tokens);
			} catch (ParseException e) {
				err(e);
			}

			lastCalculation = calc;

			runCalculation(calc);

		}

	}

	private static void runCalculation(Calculation calc) {
		//			calc.getProgram().forEach(n-> out("  " + n.toString()));
		try {
			BigDecimal res = calc.evaluate(context);
			if (calc.getResultVar() != null) {
				context.setVar(calc.getResultVar(), res);
				out(calc.getResultVar() + " := " + calc.evaluate(context));
			} else {
				out("Result: " + calc.evaluate(context));
			}
		} catch (Throwable th) {
			err(th);
		}
	}

	private static void doCommand(List<Token> list) {
		Iter<Token> iter = new Iter<>(list);
		try {
			Token t = iter.next();
			if ("q".equals(t.getValue()) || "quit".equals(t.getValue())) {
				out("Bye");
				terminated = true;
			} else if ("?".equals(t.getValue()) || "help".equals(t.getValue())) {
				out("");
				out("");
				out("[Commands] ");
				out("---------- ");
				out("");
				out("   :help  (:?)   Show this");
				out("   :quit  (:q)   Exit");
				out("   :hex   (:h)   Switch to Base 16 display ");
				out("   :dec   (:d)   Switch to Base 10 display (Default)");
				out("");
				out("   Enter an expression to evaluate it.");
				out("");
				out("     eg.  4 * (25 + 2)  <enter>");
				out("");
				out("   You can store results in variables:");
				out("          a = -45.0 + 12.3 <enter>");
				out("          50 + a <enter>");
				out("");
				out("   Integers can be entered using base16:");
				out("         0x5f + 22");
				out("");

			} else if ("h".equals(t.getValue()) || "hex".equals(t.getValue())) {
				context.setBase(16);
				out("Output in Base 16");
			} else if ("d".equals(t.getValue()) || "dec".equals(t.getValue())) {
				context.setBase(10);
				out("Output in Base 10");
			}
		} catch (Throwable th) {
			err(th);
		}
	}

	private static void out(String s) {
		System.out.println(s);
	}

	private static void err(Throwable th) {
		System.err.println("ERROR: " + th.getMessage());
	}

}
