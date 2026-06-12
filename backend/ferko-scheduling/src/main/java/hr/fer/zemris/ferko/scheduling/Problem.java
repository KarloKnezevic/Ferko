package hr.fer.zemris.ferko.scheduling;

/**
 * A discrete assignment problem solvable by the genetic algorithm. A solution is an {@code int[]}
 * of length {@link #geneCount()} where gene {@code i} holds an option index in {@code [0,
 * optionCount(i))}. Penalty is non-negative and lower is better; {@code 0} denotes a perfect
 * (conflict-free, feasible) solution.
 *
 * <p>This mirrors the chromosome representation used in Marko Čupić's thesis (a per-item group
 * index) generalised to both exam-room seating and exam-timetable conflict minimisation.
 */
public interface Problem {

  int geneCount();

  int optionCount(int gene);

  double penalty(int[] genes);
}
