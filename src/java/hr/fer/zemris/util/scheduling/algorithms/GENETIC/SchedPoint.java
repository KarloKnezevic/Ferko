package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

public class SchedPoint {
	private boolean fixed;
	private boolean occupied;

	private GenTerm term;

	public SchedPoint(boolean occupied, boolean fixed, GenTerm term) {
		this.fixed = fixed;
		this.occupied = occupied;
		this.term = term;
	}

	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public boolean isOccupied() {
		return occupied;
	}

	public void setOccupied(boolean occupied) {
		this.occupied = occupied;
	}

	public GenTerm getTerm() {
		return term;
	}

	public void setTerm(GenTerm term) {
		this.term = term;
	}
	
	@Override
	public String toString() {
		return (this.occupied + " " + this.fixed + " " + term);
	}
}
