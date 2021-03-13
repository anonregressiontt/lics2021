import java.math.BigInteger;
import java.util.Comparator;

class MinBoxMCompareD implements Comparator<MinBox> {

	public static int compareDerivatives(MinBox a, MinBox b) {
		double da = 0;
		double db = 0;
		for (Double d : a.getAbsDerivatives())
			da += d;
		for (Double d : b.getAbsDerivatives())
			db += d;
		if (da == db)
			return 0;
		double min = Math.min(da, db);
		if (min == da)
			return -1;
		return 1;
	}

	public int compare(MinBox a, MinBox b) {
		int c = MinBoxMCompareD.compareDerivatives(a, b);
		if (c == 0)
			return MinBoxCompare.compareIntervalsLower(a.getOutput(), b.getOutput());
		return c;
	}
}

class MinBoxCompare implements Comparator<MinBox> {

	public int compare(MinBox a, MinBox b) {
		if (a.getOutput().getPrecision() == b.getOutput().getPrecision()) {
			int c = a.getOutput().getBigInt().compareTo(b.getOutput().getBigInt());
			if (c == 0)
				return MinBoxMCompareD.compareDerivatives(a, b);
			return c;
		} else {
			return compareIntervalsLower(a.getOutput(), b.getOutput());
		}
	}

	public static int compareIntervalsLower(CRIntervalCode a, CRIntervalCode b) {
		BigInteger scaled_output, fixed_output;
		int scaled_outPrecision, fixed_outPrecision;
		if (a.getPrecision() == b.getPrecision()) {
			return a.getBigInt().compareTo(b.getBigInt());
		}
		if (a.getPrecision() < b.getPrecision()) {
			fixed_output = a.getBigInt();
			fixed_outPrecision = a.getPrecision();
			scaled_output = b.getBigInt();
			scaled_outPrecision = b.getPrecision();
		} else {
			fixed_output = b.getBigInt();
			fixed_outPrecision = b.getPrecision();
			scaled_output = a.getBigInt();
			scaled_outPrecision = a.getPrecision();
		}
		scaled_output = scaled_output.add(scaled_output).add(BigInteger.ONE.negate());
		scaled_outPrecision--;
		while (scaled_outPrecision != fixed_outPrecision) {
			scaled_output = scaled_output.add(scaled_output);
			scaled_outPrecision--;
		}
		if (a.getPrecision() < b.getPrecision()) {
			int c = fixed_output.compareTo(scaled_output);
			if (c == 0)
				return -1;
			return c;
		} else {
			int c = scaled_output.compareTo(fixed_output);
			if (c == 0)
				return 1;
			return c;
		}
	}

	public static int compareIntervalsUpper(CRIntervalCode a, CRIntervalCode b) {
		BigInteger scaled_output, fixed_output;
		int scaled_outPrecision, fixed_outPrecision;
		if (a.getPrecision() == b.getPrecision()) {
			return a.getBigInt().compareTo(b.getBigInt());
		}
		if (a.getPrecision() < b.getPrecision()) {
			fixed_output = a.getBigInt();
			fixed_outPrecision = a.getPrecision();
			scaled_output = b.getBigInt();
			scaled_outPrecision = b.getPrecision();
		} else {
			fixed_output = b.getBigInt();
			fixed_outPrecision = b.getPrecision();
			scaled_output = a.getBigInt();
			scaled_outPrecision = a.getPrecision();
		}
		scaled_output = scaled_output.add(scaled_output).add(BigInteger.ONE);
		scaled_outPrecision--;
		while (scaled_outPrecision != fixed_outPrecision) {
			scaled_output = scaled_output.add(scaled_output);
			scaled_outPrecision--;
		}
		if (a.getPrecision() < b.getPrecision()) {
			int c = fixed_output.compareTo(scaled_output);
			if (c == 0)
				return 1;
			return c;
		} else {
			int c = scaled_output.compareTo(fixed_output);
			if (c == 0)
				return -1;
			return c;
		}
	}

	public static boolean AEclipsesB(MinBox a, MinBox b) {
		BigInteger scaled_output, fixed_output;
		int scaled_outPrecision, fixed_outPrecision;
		if (a.getOutput().getPrecision() < b.getOutput().getPrecision()) {
			fixed_output = a.getOutput().getBigInt();
			fixed_outPrecision = a.getOutput().getPrecision();
			scaled_output = b.getOutput().getBigInt();
			scaled_outPrecision = b.getOutput().getPrecision();
		} else {
			fixed_output = b.getOutput().getBigInt();
			fixed_outPrecision = b.getOutput().getPrecision();
			scaled_output = a.getOutput().getBigInt();
			scaled_outPrecision = a.getOutput().getPrecision();
		}
		int diff = 0;
		while (scaled_outPrecision != fixed_outPrecision) {
			scaled_output = scaled_output.add(scaled_output);
			scaled_outPrecision--;
			diff = diff * 2;
			if (diff == 0)
				diff = 1;
		}
		if (diff == 0)
			diff = 1;
		if (a.getOutput().getPrecision() < b.getOutput().getPrecision()) {
			if (fixed_output.compareTo(scaled_output) < 0) {
				fixed_output = fixed_output.add(BigInteger.valueOf(diff));
				return fixed_output.compareTo(scaled_output) < 0;
			}
		} else {
			if (scaled_output.compareTo(fixed_output) < 0) {
				scaled_output = scaled_output.add(BigInteger.valueOf(diff));
				return scaled_output.compareTo(fixed_output) < 0;
			}
		}
		return false;
	}

}