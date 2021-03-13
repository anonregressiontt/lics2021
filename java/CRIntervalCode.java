

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class CRIntervalCode {

	// A real interval is encoded by a BigInteger k (for 'code') and an integer p
	// (for 'precision')
	private BigInteger k;
	private int p;

	// The interval represented by (k,p) is [(2^n*k)-(2^{p-1}) , (2^n*k)+(2^{p-1})]
	public CRIntervalCode(BigInteger k, int p) {
		this.k = k;
		this.p = p;
	}

	// Returns the BigInteger code k
	public BigInteger getBigInt() {
		return k;
	}

	// Returns the precision p
	public int getPrecision() {
		return p;
	}

	// Returns a CR object representing the interval
	// When used in functions, this CR acts as the centre of the interval
	public CR getCR() {
		return CR.valueOf(k).shiftLeft(p);
	}

	// Returns a string giving the centre of the interval, e.g. (2,1).midString() =
	// "4"
	public String midString() {
		return getCR().toString(Integer.max(0, -p));
	}

	// Returns a string stating what interval this object represents, e.g.
	// (2,1).toString() = "[3,5]"
	public String toString() {
		return "[" + goDown(-1).midString() + "," + goDown(+1).midString() + "]" + " (" + getBigInt() + ","
				+ getPrecision() + ")";
	}

	// Returns a code from the next layer down in the brick pattern, offset by i
	public CRIntervalCode goDown(int i) {
		return new CRIntervalCode(k.add(k).add(BigInteger.valueOf(i)), p - 1);
	}

	// Returns a list containing the three codes that are below this code in the
	// brick pattern
	public List<CRIntervalCode> branch() {
		return Arrays.asList(goDown(-1), goDown(0), goDown(+1));
	}

	// Returns true if the two codes represent the exact same interval, and false
	// otherwise
	public boolean sameAs(CRIntervalCode c) {
		return (k.compareTo(c.getBigInt()) == 0) && (p == c.getPrecision());
	}

	// i is the difference, i.e. n-i where n is int of the initial code
	public boolean inInterval(CRIntervalCode initial) {
		int i = initial.getPrecision() - p;
		if (i <= 0)
			return sameAs(initial);
		BigInteger n = BigInteger.valueOf(2).pow(i).multiply(initial.getBigInt());
		BigInteger lowerLimit = n.subtract(BigInteger.valueOf(2).pow(i - 1));
		BigInteger upperLimit = n.add(BigInteger.valueOf(2).pow(i - 1));
		boolean bool = (k.compareTo(lowerLimit) >= 0) && (k.compareTo(upperLimit) <= 0);
		return bool;
	}

}
