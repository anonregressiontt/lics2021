
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/*
 * An encoding of a real interval via Boehm encodings for real numbers.
 * A real interval is encoded by a BigInteger k (for 'code') and an integer p (for 'precision level')
 * The interval represented by (k,p) is [2k-1*2^{p-1} , 2k+1*2^{p-1}]
 */
public class CRIntervalCode {

	private BigInteger k;
	private int p;

	// Construct an encoding (k,p)
	public CRIntervalCode(BigInteger k, int p) {
		this.k = k;
		this.p = p;
	}

	// Returns the BigInteger code k
	public BigInteger getBigInt() {
		return k;
	}

	// Returns the precision level p
	public int getPrecision() {
		return p;
	}

	// Returns a CR object representing the centre of the interval
	public CR getCR() {
		return CR.valueOf(k).shiftLeft(p);
	}

	// Returns a string representation of the real at the centre of the interval
	// e.g. (2,1).midString = "4"
	public String midString() {
		return getCR().toString(Integer.max(0, -p));
	}

	// Returns a string representation of the interval
	// e.g (2,1).toString() = "[3,5]"
	public String toString() {
		return "[" + goDown(-1).midString() + "," + goDown(+1).midString() + "]" + " (" + getBigInt() + ","
				+ getPrecision() + ")";
	}

	// Returns a code from the next precision level, offset by i
	public CRIntervalCode goDown(int i) {
		return new CRIntervalCode(k.add(k).add(BigInteger.valueOf(i)), p - 1);
	}

	// Returns a list containing the three interval representations in the next
	// precision level that contain any members of this represented interval
	public List<CRIntervalCode> branch() {
		return Arrays.asList(goDown(-1), goDown(0), goDown(+1));
	}

	// Returns true only if the two codes represent the exact same interval
	public boolean sameAs(CRIntervalCode c) {
		return (k.compareTo(c.getBigInt()) == 0) && (p == c.getPrecision());
	}

	// Returns true only if all members of this represented interval are also in the
	// parameter's represented interval
	public boolean inInterval(CRIntervalCode c) {
		int i = c.getPrecision() - p;
		if (i <= 0)
			return sameAs(c);
		BigInteger n = BigInteger.valueOf(2).pow(i).multiply(c.getBigInt());
		BigInteger lowerLimit = n.subtract(BigInteger.valueOf(2).pow(i - 1));
		BigInteger upperLimit = n.add(BigInteger.valueOf(2).pow(i - 1));
		boolean bool = (k.compareTo(lowerLimit) >= 0) && (k.compareTo(upperLimit) <= 0);
		return bool;
	}

}
