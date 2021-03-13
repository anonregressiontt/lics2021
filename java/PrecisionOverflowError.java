/**
 * Indicates that the number of bits of precision requested by a computation on
 * constructive reals required more than 28 bits, and was thus in danger of
 * overflowing an int. This is likely to be a symptom of a diverging
 * computation, <I>e.g.</i> division by zero.
 */
public class PrecisionOverflowError extends Error {
	public PrecisionOverflowError() {
		super();
	}

	public PrecisionOverflowError(String s) {
		super(s);
	}
}
