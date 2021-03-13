import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * A search candidate for optimising a multivariate function on Boehm encodings for real numbers.
 * The candidate consists of: 
 *   The CRFunctionModulus representation of the function to be optimised,
 *   A list of functions representing partial derivatives of the function,
 *   A list of input interval codes representing the inputs to the function,
 *   A list of output interval codes representing the whole output of the function applies to the input,
 *   A list of partial derivative heuristic values to help guide the search process.
*/
class MinBox {

	private List<CRIntervalCode> initials;
	private List<CRIntervalCode> inputs;
	private List<CRIntervalCode> output;
	private List<Double> derivatives;
	private CRFunctionModulus f;
	private List<CRFunctionModulus> ds;

	// Constructor uses the 'apply' method of the function representation to compute
	// the output intervals
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

	// Branch a search candidate in one dimension to return a list of search
	// candidates whose inputs are at the next level of precision and completely
	// cover the original candidate in that dimension
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

	// Return the list of new candidates given by branching the search candidate in
	// all dimensions via branchArg()
	public List<MinBox> branchAll() {
		List<MinBox> newBoxes = new ArrayList<>();
		newBoxes.add(this);
		for (int i = 0; i < inputs.size(); i++) {
			final int j = i;
			newBoxes = newBoxes.stream().map(x -> x.branchArg(j)).flatMap(x -> x.stream()).collect(Collectors.toList());
		}
		return newBoxes;
	}

	// Return those search candidates in a given list that cannot contain a global
	// minimiser
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

	// Return the input intervals of a search candidate
	public List<CRIntervalCode> getInputs() {
		return inputs;
	}

	// Return the output intervals of a search candidate
	public List<CRIntervalCode> getOutput() {
		return output;
	}

	// Return the partial derivative heuristic value of the i-th dimension of a
	// search candidate
	public double getDerivative(int i) {
		return derivatives.get(i);
	}

	// Return all partial derivative heuristic values for this search candidate
	public List<Double> getDerivatives() {
		return derivatives;
	}

	// Returns true only if all inputs of the two search candidates are identical
	public boolean sameAs(MinBox box) {
		for (int i = 0; i < inputs.size(); i++) {
			if (!box.getInputs().get(i).sameAs(inputs.get(i))) {
				return false;
			}
		}
		return true;
	}

	// Returns true only if there is an identical search candidate in the list
	public boolean alreadyIn(List<MinBox> history) {
		for (MinBox box : history) {
			if (sameAs(box))
				return true;
		}
		return false;
	}

	// Returns a string used for printing information about the search candidate
	public String toString() {
		return getInputs().toString() + " ==> " + getOutput().toString() + " -- " + getDerivatives() + "\n";
	}

	// Returns a string for the interval represented by the entire input space's
	// i-th dimension of a given list of search candidates
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

	// Returns a string for the interval represented by the entire output space of a
	// given list of search candidates
	public static String unionOutput(ArrayList<MinBox> frontier) {
		List<CRIntervalCode> output = frontier.get(0).getOutput();
		CRIntervalCode lower = output.get(0);
		CRIntervalCode upper = output.get(output.size() - 1);
		for (int j = 1; j < frontier.size(); j++) {
			output = frontier.get(j).getOutput();
			CRIntervalCode maybeLower = output.get(0);
			CRIntervalCode maybeUpper = output.get(output.size() - 1);
			if (MinBoxCompare.compareIntervalsLower(maybeLower, lower) == -1)
				lower = maybeLower;
			if (MinBoxCompare.compareIntervalsUpper(upper, maybeUpper) == -1)
				upper = maybeUpper;
		}
		return "[" + lower.goDown(-1).midString() + "," + upper.goDown(+1).midString() + "]";
	}

	// Returns a string detailing the entire search area of a given list of search
	// candidates
	public static String unionFrontier(ArrayList<MinBox> frontier, int numParams) {
		String unionStr = "f(";
		for (int i = 0; i < numParams; i++) {
			unionStr += unionParam(frontier, i);
		}
		unionStr += (") ==> " + unionOutput(frontier));
		return unionStr;
	}

	// Return all absolute partial derivative heuristic values for this search
	// candidate
	public List<Double> getAbsDerivatives() {
		List<Double> absds = new ArrayList<>();
		for (Double d : getDerivatives()) {
			absds.add(Math.abs(d));
		}
		return absds;
	}

}