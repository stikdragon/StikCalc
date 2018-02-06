package uk.co.stikman.calc;


import java.util.ArrayList;
import java.util.List;


public class Iter<T> {
	public interface ToStringFormatter<K> {
		String format(K t);
	}

	public interface SkipRule<Q> {
		boolean skip(Q x);
	}

	public interface Mark {
		void restore();
	}

	private List<T>					source;
	private int						pos;
	private T						outOfBoundValue	= null;
	private List<SkipRule<T>>		skipRules		= new ArrayList<>();
	private T						last			= null;
	private ToStringFormatter<T>	formatter		= x -> x == null ? "null" : x.toString();

	public Iter(List<T> source) {
		this.source = source;
		this.pos = -1;
	}

	/**
	 * Returns the current value, if it's out of bounds it'll return the default
	 * value
	 * 
	 * @return
	 */
	public T peek() {
		if (pos < 0 || pos >= source.size()) {
			if (outOfBoundValue == null)
				throw new IndexOutOfBoundsException("End of list");
			return outOfBoundValue;
		}
		return source.get(pos);
	}

	public boolean hasNext() {
		return pos < source.size() - 1;
	}

	public boolean hasPrev() {
		return pos > 0;
	}

	public T peekNext() {
		int p = pos + 1;
		while (true) {
			if (p >= source.size())
				return outOfBoundValue;
			T t = source.get(p++);
			boolean b = false;
			for (SkipRule<T> r : skipRules)
				b |= r.skip(t);
			if (b)
				continue;
			return t;
		}
	}

	public T next() {
		if (pos < 0)
			last = null;
		else
			last = peek();
		while (true) {
			++pos;
			if (pos >= source.size()) {
				if (outOfBoundValue == null)
					throw new IndexOutOfBoundsException();
				return outOfBoundValue;
			}
			T t = source.get(pos);
			boolean b = false;
			for (SkipRule<T> r : skipRules)
				b |= r.skip(t);
			if (b)
				continue;
			return t;
		}
	}

	public List<T> getList() {
		return source;
	}

	public int getPosition() {
		return pos;
	}

	public void setPosition(int pos) {
		this.pos = pos;
	}

	public Mark mark() {
		final int p = pos;
		return new Mark() {
			@Override
			public void restore() {
				pos = p;
			}
		};
	}

	/**
	 * This will be returned on accesses to invalid locations. If it's
	 * <code>null</code> then many methods throw exceptions instead. Default is
	 * <code>null</code>
	 * 
	 * @return
	 */
	public T getOutOfBoundValue() {
		return outOfBoundValue;
	}

	public void setOutOfBoundValue(T outOfBoundValue) {
		this.outOfBoundValue = outOfBoundValue;
	}

	@Override
	public String toString() {
		if (pos < 0)
			return "Prev: null, Current: null, Next: " + formatter.format(peekNext());
		return "Prev: " + formatter.format(last) + ", Current: " + formatter.format(peek()) + ", Next: " + formatter.format(peekNext());
	}

	/**
	 * Anything in the skip rule list behaves as if it wasn't in the list at all
	 * 
	 * @param rule
	 */
	public void addSkipRule(SkipRule<T> rule) {
		skipRules.add(rule);
	}

	public void clearSkipRules() {
		skipRules.clear();
	}

	public ToStringFormatter<T> getFormatter() {
		return formatter;
	}

	public void setFormatter(ToStringFormatter<T> formatter) {
		this.formatter = formatter;
	}
}
