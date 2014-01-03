package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FitnessResult implements Comparable<FitnessResult> {
	public List<Float> results;

	public FitnessResult() {
		this.results = new ArrayList<Float>();
	}

	public FitnessResult(List<Float> input) {
		this.results = input;
	}

	public void add(float val) {
		this.results.add(val);
	}

	public void setValue(int pos, float val) {
		results.set(pos, val);
	}

	public float getValue(int pos) {
		return results.get(pos);
	}

	@Override
	public int compareTo(FitnessResult o) {
		int length;
		int moreDetails;

		if (this.results.size() >= o.results.size()) {
			length = o.results.size();
			moreDetails = -1;

			if (this.results.size() == o.results.size()) {
				moreDetails = 0;
			}
		} else {
			length = this.results.size();
			moreDetails = 1;
		}

		// ispituju se vrijednosti svih velicina
		for (int i = 0; i < length; i++) {
			if (this.getValue(i) != o.getValue(i)) {
				if (this.getValue(i) > o.getValue(i))
					return 1;
				else
					return -1;
			}
		}

		// ako nije nadjeno
		return moreDetails;
	}

	@Override
	public String toString() {
		String s = "";

		for (float value : results)
			s += value + " ";

		return s;
	}
}
