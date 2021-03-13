import java.util.List;
import java.util.stream.Collectors;

import javafx.util.Pair;

public abstract class CRFunctionModulus {

	abstract public CR function(List<CR> xs);

	abstract public CR modulus(List<CR> xs, List<CR> es);

	public int nearestPowOfTwo(CR c) {
		return c.ln().divide(CR.two.ln()).add(CR.valueOf(0.5)).intValue() - 1;
	}

	public CR applyCR(List<CRIntervalCode> xs) {
		List<CR> centres = xs.stream().map(x -> x.getCR()).collect(Collectors.toList());
		return function(centres);
	}

	// Returns the output code of a multivariate function given some inputs, i.e.
	// truncated using modulus information
	public CRIntervalCode apply(List<CRIntervalCode> xs) {
		List<CR> centres = xs.stream().map(x -> x.getCR()).collect(Collectors.toList());
		List<CR> distances = xs.stream().map(x -> CR.two.pow(x.getPrecision() - 1)).collect(Collectors.toList());
		int outPrecision = nearestPowOfTwo(modulus(centres, distances));
		CR fxs = function(centres);
		return new CRIntervalCode(fxs.get_appr(outPrecision), outPrecision);
	}

	public static final CRFunctionModulus constant(CR y) {
		return new constant_CRFunctionModulus(y);
	}

	public static final CRFunctionModulus proj(int i) {
		return new proj_CRFunctionModulus(i);
	}

	public static final CRFunctionModulus addFG(CRFunctionModulus f, CRFunctionModulus g) {
		return new addFG_CRFunctionModulus(f, g);
	}

	public static final CRFunctionModulus pow(int i, int n) { // xs(i)^n , n >= 0 , i < xs.size()
		CRFunctionModulus f = constant(CR.one);
		if (n == 1)
			f = proj(i);
		if (n > 1) {
			CRFunctionModulus g = pow(i, n / 2);
			CRFunctionModulus h = pow(i, n - (n / 2));
			f = new timesFG_CRFunctionModulus(g, h);
		}
		return f;
	}

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