import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.util.Pair;

/*
 * Abstract class for representing multivariate functions on Boehm encodings for real numbers.
 * A function 'f : R^n -> R' with modulus of continuity 'm : R^n x R^n -> R'
 * is represented by two methods 'function : CR^n -> CR' and 'modulus : [CR] x [CR] -> CR'.
 * These should satisfy { function(xs) } = f({xs}) and { modulus(xs,es) } = m({xs},{es}),
 * where {-} is the realisability mapping CR^n -> R^n.
 */
public abstract class CRFunctionModulus {

	abstract public CR function(List<CR> xs);

	abstract public CR modulus(List<CR> xs, List<CR> es);

	// Gives the next biggest n such that {c} <= 2^n
	public int nearestPowOfTwo(CR c) {
		return c.ln().divide(CR.two.ln()).add(CR.valueOf(0.5)).intValue();
	}

	// Returns the output of function(xs)
	public CR applyCR(List<CRIntervalCode> xs) {
		List<CR> centres = xs.stream().map(x -> x.getCR()).collect(Collectors.toList());
		return function(centres);
	}

	// Gives back a list of intervals representing the whole output space of the
	// function given an input interval (computed via the modulus)
	public List<CRIntervalCode> apply(List<CRIntervalCode> xs) {
		List<CR> centres = xs.stream().map(x -> x.getCR()).collect(Collectors.toList());
		List<CR> distances = xs.stream().map(x -> CR.two.pow(x.getPrecision() - 1)).collect(Collectors.toList());
		int outPrecision = nearestPowOfTwo(modulus(centres, distances)) - 1;
		CR fxs = function(centres);
		CRIntervalCode low = new CRIntervalCode(fxs.get_appr(outPrecision).subtract(BigInteger.ONE), outPrecision);
		CRIntervalCode mid = new CRIntervalCode(fxs.get_appr(outPrecision), outPrecision);
		CRIntervalCode upp = new CRIntervalCode(fxs.get_appr(outPrecision).add(BigInteger.ONE), outPrecision);
		List<CRIntervalCode> list = new ArrayList<>();
		list.add(low);
		list.add(mid);
		list.add(upp);
		return list;
	}

	// Returns a function representing the constant function \{x_0..x_n}.y
	public static final CRFunctionModulus constant(CR y) {
		return new constant_CRFunctionModulus(y);
	}

	// Returns a function representing the constant function \{x_0..x_n}.x_i
	public static final CRFunctionModulus proj(int i) {
		return new proj_CRFunctionModulus(i);
	}

	// Returns a function representing the addition function
	// \{x_0..x_n}.f(x_0..xn)+g(x_0..x_n)
	// where f and g are objects of CRFunctionModulus
	public static final CRFunctionModulus addFG(CRFunctionModulus f, CRFunctionModulus g) {
		return new addFG_CRFunctionModulus(f, g);
	}

	// Returns a function representing the power function
	// \{x_0..x_n}.(x_i)^j for given i <= n and n >= 0
	public static final CRFunctionModulus pow(int i, int j) {
		CRFunctionModulus f = constant(CR.one);
		if (j == 1)
			f = proj(i);
		if (j > 1) {
			CRFunctionModulus g = pow(i, j / 2);
			CRFunctionModulus h = pow(i, j - (j / 2));
			f = new timesFG_CRFunctionModulus(g, h);
		}
		return f;
	}

	// Returns a function representing the polynomial
	// \{x_0..x_n}.Sum_i(li[0]*((x_li[1])^li[2]))
	// where li =
	// [l.get(i).getKey(),l.get(i).getValue.getKey(),l.get(i).getValue().getValue()]
	public static CRFunctionModulus polynomial(List<Pair<Double, Pair<Integer, Integer>>> l) {
		// Polynomial in i variables
		CRFunctionModulus f = constant(CR.valueOf(0));
		for (int j = 0; j < l.size(); j++) {
			// g = a * i ^ k
			Double a = l.get(j).getKey();
			Integer i = l.get(j).getValue().getKey();
			Integer k = l.get(j).getValue().getValue();
			CRFunctionModulus g = constant(CR.valueOf(a));
			CRFunctionModulus h = pow(i, k);
			f = addFG(f, new timesFG_CRFunctionModulus(g, h));
		}
		return f;
	}

}

// Subclass for representing constant functions
class constant_CRFunctionModulus extends CRFunctionModulus {
	private CR constant;

	public constant_CRFunctionModulus(CR c) {
		constant = c;
	}

	public CR function(List<CR> xs) {
		return constant;
	}

	public CR modulus(List<CR> xs, List<CR> es) {
		return CR.valueOf(0);
	}
}

// Subclass for representing projection functions
class proj_CRFunctionModulus extends CRFunctionModulus {
	private int i;

	public proj_CRFunctionModulus(int i) {
		this.i = i;
	}

	public CR function(List<CR> xs) {
		return xs.get(i);
	}

	public CR modulus(List<CR> xs, List<CR> es) {
		return es.get(i);
	}
}

// Subclass for representing binary addition functions
class addFG_CRFunctionModulus extends CRFunctionModulus {
	private CRFunctionModulus f;
	private CRFunctionModulus g;

	public addFG_CRFunctionModulus(CRFunctionModulus f, CRFunctionModulus g) {
		this.f = f;
		this.g = g;
	}

	public CR function(List<CR> xs) {
		return f.function(xs).add(g.function(xs));
	}

	public CR modulus(List<CR> xs, List<CR> es) {
		return f.modulus(xs, es).add(g.modulus(xs, es));
	}
}

// Subclass for representing binary multiplication functions
class timesFG_CRFunctionModulus extends CRFunctionModulus {
	private CRFunctionModulus f;
	private CRFunctionModulus g;

	public timesFG_CRFunctionModulus(CRFunctionModulus f, CRFunctionModulus g) {
		this.f = f;
		this.g = g;
	}

	public CR function(List<CR> xs) {
		return f.function(xs).multiply(g.function(xs));
	}

	public CR modulus(List<CR> xs, List<CR> es) {
		CR fx = f.function(xs).abs();
		CR gx = g.function(xs).abs();
		CR mx = f.modulus(xs, es);
		CR nx = g.modulus(xs, es);
		CR one = gx.multiply(mx);
		CR two = fx.multiply(nx);
		CR three = mx.multiply(nx);
		return one.add(two).add(three);
	}
}