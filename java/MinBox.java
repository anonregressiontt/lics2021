import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

class MinBox {

	private List<CRIntervalCode> initials;
	private List<CRIntervalCode> inputs;
	private CRIntervalCode output;
	private List<Double> derivatives;
	private boolean toBeRemoved = false;
	private CRFunctionModulus f;
	private List<CRFunctionModulus> ds;

	public MinBox(List<CRIntervalCode> initialInputs, List<CRIntervalCode> currentInputs, CRFunctionModulus f,
			List<CRFunctionModulus> ds) {
		this.initials = initialInputs;
		this.inputs = currentInputs;
		this.f = f;
		this.ds = ds;
		this.output = f.apply(inputs);
		List<Double> derivatives = new ArrayList<>();
		for (CRFunctionModulus d : ds) {
			derivatives.add(d.applyCR(inputs).doubleValue());
		}
		this.derivatives = derivatives;
	}

	public List<MinBox> branchArg(int i) {
		List<MinBox> newBoxes = new ArrayList<>();
		for (CRIntervalCode newArg : inputs.get(i).branch()) {
			if (newArg.inInterval(initials.get(i))) {
				List<CRIntervalCode> replacedArgs = new ArrayList<>(inputs);
				replacedArgs.set(i, newArg);
				newBoxes.add(new MinBox(initials, replacedArgs, f, ds));
			}
		}
		return newBoxes;
	}

	public List<MinBox> branchAll() {
		List<MinBox> newBoxes = new ArrayList<>();
		newBoxes.add(this);
		for (int i = 0; i < inputs.size(); i++) {
			final int j = i;
			newBoxes = newBoxes.stream().map(x -> x.branchArg(j)).flatMap(x -> x.stream()).collect(Collectors.toList());
		}
		return newBoxes;
	}

	public static ArrayList<MinBox> getEclipsed(ArrayList<MinBox> list) {
		ArrayList<MinBox> noMinimum = new ArrayList<>();
		for (MinBox arg0 : list) {
			for (MinBox arg1 : list) {
				if (!noMinimum.contains(arg0) && MinBoxCompare.AEclipsesB(arg1, arg0)) {
					noMinimum.add(arg0);
				}
			}
		}
		return noMinimum;
	}

	public static ArrayList<MinBox> removeEclipsed(ArrayList<MinBox> list) {
		Iterator<MinBox> args1 = list.iterator();
		for (MinBox arg0 : list) {
			while (args1.hasNext() && !arg0.toBeRemoved) {
				MinBox arg1 = args1.next();
				arg0.toBeRemoved = MinBoxCompare.AEclipsesB(arg1, arg0);
				arg1.toBeRemoved = MinBoxCompare.AEclipsesB(arg0, arg1);
			}
		}
		list.removeIf(x -> x.toBeRemoved);
		return list;
	}

	public List<CRIntervalCode> getInputs() {
		return inputs;
	}

	public CRIntervalCode getOutput() {
		return output;
	}

	public double getDerivative(int i) {
		return derivatives.get(i);
	}

	public List<Double> getDerivatives() {
		return derivatives;
	}

	public boolean sameAs(MinBox box) {
		for (int i = 0; i < inputs.size(); i++) {
			if (!box.getInputs().get(i).sameAs(inputs.get(i))) {
				return false;
			}
		}
		return true;
	}

	public boolean alreadyIn(List<MinBox> history) {
		for (MinBox box : history) {
			if (sameAs(box))
				return true;
		}
		return false;
	}

	public String toString() {
		return getInputs().toString() + " ==> " + getOutput().toString() + " -- " + getDerivatives() + "\n";
	}

	public static String unionParam(ArrayList<MinBox> frontier, int i) {
		CRIntervalCode lower = frontier.get(0).getInputs().get(i);
		CRIntervalCode upper = frontier.get(0).getInputs().get(i);
		for (int j = 1; j < frontier.size(); j++) {
			CRIntervalCode current = frontier.get(j).getInputs().get(i);
			if (MinBoxCompare.compareIntervalsLower(current, lower) == -1)
				lower = current;
			if (MinBoxCompare.compareIntervalsUpper(upper, current) == -1)
				upper = current;
		}
		return "[" + lower.goDown(-1).midString() + "," + upper.goDown(+1).midString() + "]";
	}

	public static String unionOutput(ArrayList<MinBox> frontier) {
		CRIntervalCode lower = frontier.get(0).getOutput();
		CRIntervalCode upper = frontier.get(0).getOutput();
		for (int j = 1; j < frontier.size(); j++) {
			CRIntervalCode current = frontier.get(j).getOutput();
			if (MinBoxCompare.compareIntervalsLower(current, lower) == -1)
				lower = current;
			if (MinBoxCompare.compareIntervalsUpper(upper, current) == -1)
				upper = current;
		}
		return "[" + lower.goDown(-1).midString() + "," + upper.goDown(+1).midString() + "]";
	}

	public static String unionFrontier(ArrayList<MinBox> frontier, int numParams) {
		String unionStr = "f(";
		for (int i = 0; i < numParams; i++) {
			unionStr += unionParam(frontier, i);
		}
		unionStr += (") ==> " + unionOutput(frontier));
		return unionStr;
	}

	public List<Double> getAbsDerivatives() {
		List<Double> absds = new ArrayList<>();
		for (Double d : getDerivatives()) {
			absds.add(Math.abs(d));
		}
		return absds;
	}

}