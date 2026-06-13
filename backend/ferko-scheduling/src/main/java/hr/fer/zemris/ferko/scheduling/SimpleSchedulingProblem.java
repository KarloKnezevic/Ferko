package hr.fer.zemris.ferko.scheduling;

/**
 * Simple scheduling of activities into time slots (Čupić, thesis 4.1). Each of {@code N} activities
 * is assigned exactly one of {@code T} time slots; gene {@code a} holds the slot index of activity
 * {@code a} and so {@link #optionCount(int)} is {@code T} for every gene.
 *
 * <p>The single hard constraint is that two activities sharing a resource (teacher, student group,
 * room, ...) must not occupy the same slot. This is expressed by a symmetric {@code N x N} {@code
 * conflict} matrix where {@code conflict[i][j]} is {@code true} when activities {@code i} and
 * {@code j} cannot be scheduled simultaneously. The penalty is the number of conflicting activity
 * pairs placed in the same slot, each violation weighted by {@code 1}. A penalty of {@code 0} means
 * every hard constraint is satisfied, i.e. a valid schedule.
 */
public final class SimpleSchedulingProblem implements Problem {

  private final int timeSlots;
  private final boolean[][] conflict;

  /**
   * Creates a simple scheduling problem.
   *
   * @param timeSlots number of available time slots, must be {@code >= 1}
   * @param conflict symmetric {@code N x N} conflict matrix; {@code conflict[i][j]} is {@code true}
   *     when activities {@code i} and {@code j} cannot share a slot
   * @throws IllegalArgumentException if {@code timeSlots < 1}, if {@code conflict} is {@code null}
   *     or not square, if any activity conflicts with itself, or if the matrix is not symmetric
   */
  public SimpleSchedulingProblem(int timeSlots, boolean[][] conflict) {
    if (timeSlots < 1) {
      throw new IllegalArgumentException("timeSlots must be >= 1");
    }
    if (conflict == null) {
      throw new IllegalArgumentException("conflict matrix must not be null");
    }
    int n = conflict.length;
    for (int i = 0; i < n; i++) {
      if (conflict[i] == null || conflict[i].length != n) {
        throw new IllegalArgumentException("conflict must be a square N x N matrix");
      }
    }
    for (int i = 0; i < n; i++) {
      if (conflict[i][i]) {
        throw new IllegalArgumentException("an activity cannot conflict with itself: " + i);
      }
      for (int j = i + 1; j < n; j++) {
        if (conflict[i][j] != conflict[j][i]) {
          throw new IllegalArgumentException("conflict matrix must be symmetric at " + i + "," + j);
        }
      }
    }
    this.timeSlots = timeSlots;
    this.conflict = deepCopy(conflict);
  }

  /** Returns the number of activities to be scheduled. */
  public int activityCount() {
    return conflict.length;
  }

  /** Returns the number of available time slots. */
  public int timeSlotCount() {
    return timeSlots;
  }

  @Override
  public int geneCount() {
    return conflict.length;
  }

  @Override
  public int optionCount(int gene) {
    return timeSlots;
  }

  @Override
  public double penalty(int[] genes) {
    if (genes == null || genes.length != conflict.length) {
      throw new IllegalArgumentException("genes must have length " + conflict.length);
    }
    int violations = 0;
    for (int i = 0; i < conflict.length; i++) {
      for (int j = i + 1; j < conflict.length; j++) {
        if (conflict[i][j] && genes[i] == genes[j]) {
          violations++;
        }
      }
    }
    return violations;
  }

  private static boolean[][] deepCopy(boolean[][] source) {
    boolean[][] copy = new boolean[source.length][];
    for (int i = 0; i < source.length; i++) {
      copy[i] = source[i].clone();
    }
    return copy;
  }
}
