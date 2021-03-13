import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javafx.util.Pair;

/*
 * The implementation of a branch-and-bound algorithm on Boehm encodings for real numbers.
 * The main method allows one to test the algorithm on any unary polynomial function.
 */
class GlobalSearchMain {

	private int timeGoal;
	private int numParameters;
	private ArrayList<MinBox> frontier = new ArrayList<>();

	// Initialise the algorithm
	public GlobalSearchMain(CRFunctionModulus function, List<CRFunctionModulus> derivative, int numParameters,
			int startPrecision, int timeGoal) {
		this.timeGoal = timeGoal;
		this.numParameters = numParameters;
		List<CRIntervalCode> initialInputs = new ArrayList<>();
		for (int i = 0; i < numParameters; i++) {
			initialInputs.add(new CRIntervalCode(BigInteger.ZERO, startPrecision));
		}
		// Initialise the search area as a single candidate
		frontier.add(new MinBox(initialInputs, initialInputs, function, derivative));
	}

	// Algorithm
	public Pair<String, Pair<CR, CR>> minimise() {
		// The first candidate to be branched is the initial candidate
		MinBox current = frontier.get(0);
		List<MinBox> history = new ArrayList<>();
		List<MinBox> noMinimum = new ArrayList<>();
		history.add(current);
		long startTime = System.nanoTime();
		long time = 0;
		String globalSearchResult = "";
		boolean flag = false;
		// Stop after a given amount of time
		while (time < timeGoal) {
			// Remove the element to be branched from the frontier
			frontier.remove(0);
			// Perform the branching process; if there are multiple parameters, this is
			// guided by the partial derivatives
			List<MinBox> newBoxes = new ArrayList<>();
			int j = 0;
			if (numParameters > 1) {
				double maxDerivative = Collections.max(current.getAbsDerivatives());
				j = current.getAbsDerivatives().indexOf(maxDerivative);
			}
			newBoxes = current.branchArg(j);
			// Check whether this box has been evaluated before, and add it to the search
			// area if not
			for (MinBox newBox : newBoxes) {
				if (!newBox.alreadyIn(history)) {
					frontier.add(newBox);
					history.add(newBox);
				}
			}
			// Sort the search area by global criteria for the first 80% of the time, and
			// local criteria (i.e. derivative heuristics) for the last 20% of the time
			time = (System.nanoTime() - startTime) / 1000000;
			if (time < timeGoal * 0.8) {
				frontier.sort(new MinBoxCompare());
			} else {
				if (!flag)
					globalSearchResult = MinBox.unionFrontier(frontier, numParameters);
				flag = true;
				frontier.sort(new MinBoxMCompareD());
			}
			// Remove all candidates that cannot contain a global minimiser
			noMinimum.addAll(MinBox.getEclipsed(frontier));
			for (MinBox box : noMinimum) {
				frontier.removeIf(x -> x.sameAs(box));
			}
			// Select the next candidate to be branched
			current = frontier.get(0);
		}
		// Return results of the global (and local) search
		return new Pair<>(globalSearchResult,
				new Pair<>(current.getInputs().get(0).getCR(), current.getOutput().get(1).getCR()));

	}

	public static void main(String[] args) {
		List<Pair<Double, Pair<Integer, Integer>>> fl = new ArrayList<>();
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter degree of polynomial: ");
		int degree = sc.nextInt();
		for (int i = degree; i >= 0; i--) {
			System.out.print("Enter parameter 'a' for term 'ax^" + i + "': ");
			double param = sc.nextDouble();
			if (param != 0) {
				fl.add(new Pair<>(param, new Pair<>(0, i)));
			}
		}
		CRFunctionModulus f = CRFunctionModulus.polynomial(fl);
		List<Pair<Double, Pair<Integer, Integer>>> dl = new ArrayList<>();
		System.out.print("Chosen polynomial: ");
		for (int i = 0; i < fl.size(); i++) {
			double fparam = fl.get(i).getKey();
			int fi = fl.get(i).getValue().getValue();
			if (fparam != 0) {
				if (i != 0) {
					System.out.print(" + ");
				}
				if (fi > 1) {
					System.out.print(fparam + "x^" + fi);
				} else if (fi == 1) {
					System.out.print(fparam + "x");
				} else {
					System.out.print(fparam);
				}
			}
			if (fi != 0) {
				double dparam = fparam * fi;
				int di = fi - 1;
				dl.add(new Pair<>(dparam, new Pair<>(0, di)));
			}
		}
		System.out.println();
		CRFunctionModulus d = CRFunctionModulus.polynomial(fl);
		List<CRFunctionModulus> ds = new ArrayList<>();
		ds.add(d);
		System.out.print("Enter 'n' value of starting interval [-2^n,2^n]: ");
		int startPrec = sc.nextInt();
		System.out.print("Enter number of seconds to run for: ");
		int time = sc.nextInt();
		sc.close();
		GlobalSearchMain min = new GlobalSearchMain(f, ds, 1, startPrec + 1, time * 1000);
		Pair<String, Pair<CR, CR>> results = min.minimise();
		String globalSearchResult = results.getKey();
		Pair<CR, CR> localSearchResult = results.getValue();
		System.out.println();
		System.out.println("Global search result: " + globalSearchResult);
		System.out.println("Local search estimate: f(" + localSearchResult.getKey() + ") " + "==> "
				+ localSearchResult.getValue());
	}
}