package hr.fer.zemris.ferko.scheduling;

/**
 * Forming seminar presentation groups (Čupić's thesis 4.8). Each of {@code N} students is assigned
 * to one of {@code G} presentation groups. Gene {@code i} = group index for student {@code i}, so
 * {@code optionCount} is {@code G} for every gene.
 *
 * <p>The penalty combines two terms:
 *
 * <ul>
 *   <li><b>Hard constraint — group capacity:</b> for every group the over-capacity {@code max(0,
 *       size - capacity)} is squared and multiplied by a large dominating weight, so any capacity
 *       violation overwhelms the preference term. A penalty contribution of 0 here means every
 *       group stays within capacity.
 *   <li><b>Soft constraint — topic preferences:</b> the sum over all students of {@code
 *       preferenceCost[i][assignedGroup]} (lower means more preferred).
 * </ul>
 *
 * A penalty of 0 means no group is over capacity and every student was placed in a zero-cost
 * (maximally preferred) group.
 */
public final class SeminarGroupsProblem implements Problem {

  /** Dominating weight applied to (squared) capacity overflow so hard constraints win. */
  private static final double CAPACITY_WEIGHT = 1_000_000.0;

  private final int[] groupCapacity;
  private final int[][] preferenceCost;

  /**
   * Creates the problem.
   *
   * @param groupCapacity capacity of each of the {@code G} groups; must be non-empty and
   *     non-negative
   * @param preferenceCost {@code N x G} matrix; {@code preferenceCost[i][g]} is the (non-negative)
   *     cost of placing student {@code i} in group {@code g}
   * @throws IllegalArgumentException if the inputs are null, ragged, mis-shaped, or negative
   */
  public SeminarGroupsProblem(int[] groupCapacity, int[][] preferenceCost) {
    if (groupCapacity == null || groupCapacity.length == 0) {
      throw new IllegalArgumentException("at least one group is required");
    }
    for (int capacity : groupCapacity) {
      if (capacity < 0) {
        throw new IllegalArgumentException("group capacity must be >= 0");
      }
    }
    if (preferenceCost == null) {
      throw new IllegalArgumentException("preferenceCost must not be null");
    }
    int groupCount = groupCapacity.length;
    for (int[] row : preferenceCost) {
      if (row == null || row.length != groupCount) {
        throw new IllegalArgumentException("preferenceCost must be studentCount x groupCount");
      }
      for (int cost : row) {
        if (cost < 0) {
          throw new IllegalArgumentException("preference cost must be >= 0");
        }
      }
    }
    this.groupCapacity = groupCapacity.clone();
    this.preferenceCost = deepCopy(preferenceCost);
  }

  /** Number of students to assign. */
  public int studentCount() {
    return preferenceCost.length;
  }

  /** Number of presentation groups. */
  public int groupCount() {
    return groupCapacity.length;
  }

  @Override
  public int geneCount() {
    return preferenceCost.length;
  }

  @Override
  public int optionCount(int gene) {
    return groupCapacity.length;
  }

  @Override
  public double penalty(int[] genes) {
    int[] size = new int[groupCapacity.length];
    double preferenceTotal = 0.0;
    for (int student = 0; student < genes.length; student++) {
      int group = genes[student];
      size[group]++;
      preferenceTotal += preferenceCost[student][group];
    }
    double capacityPenalty = 0.0;
    for (int group = 0; group < size.length; group++) {
      int overflow = size[group] - groupCapacity[group];
      if (overflow > 0) {
        capacityPenalty += Math.pow(overflow, 2);
      }
    }
    return CAPACITY_WEIGHT * capacityPenalty + preferenceTotal;
  }

  private static int[][] deepCopy(int[][] source) {
    int[][] copy = new int[source.length][];
    for (int i = 0; i < source.length; i++) {
      copy[i] = source[i].clone();
    }
    return copy;
  }
}
