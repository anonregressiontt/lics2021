/**
 * Indicates a constructive real operation was interrupted. Most constructive
 * real operations may throw such an error. This is an error, since Number
 * methods may not raise such exceptions.
 */
public class AbortedError extends Error {
	public AbortedError() {
		super();
	}

	public AbortedError(String s) {
		super(s);
	}
}
