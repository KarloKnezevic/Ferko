package hr.fer.zemris.ferko.scheduling;

/**
 * Partition of {@code N} people into {@code K} teams of bounded size, balancing the per-team total
 * weight (workload or skill). Gene {@code i} = team index for person {@code i}; {@link
 * #optionCount(int)} = {@code teamCount}. Following Čupić's team-scheduling objective (thesis 4.7)
 * the penalty has two additive parts:
 *
 * <ul>
 *   <li><b>size violation</b>: per team {@code max(0, size - maxSize)^2 + max(0, minSize -
 *       size)^2}, penalising teams that are too large or too small;
 *   <li><b>imbalance</b>: the sum over teams of the squared deviation of the team's total weight
 *       from the average team weight ({@code totalWeight / teamCount}).
 * </ul>
 *
 * A penalty of {@code 0} means every team respects its size bounds and all teams carry exactly the
 * average weight, i.e. the partition is perfectly balanced and size-valid. Both components are
 * quadratic (squared) so larger deviations are penalised non-linearly.
 */
public final class TeamSchedulingProblem implements Problem {

  private final int teamCount;
  private final int[] personWeight;
  private final int minSize;
  private final int maxSize;

  /**
   * Creates a team-scheduling problem.
   *
   * @param teamCount number of teams ({@code >= 1})
   * @param personWeight weight (workload/skill) of each person; non-null, each entry {@code >= 0}
   * @param minSize minimum allowed team size ({@code >= 0})
   * @param maxSize maximum allowed team size ({@code >= minSize})
   * @throws IllegalArgumentException if any argument is out of range
   */
  public TeamSchedulingProblem(int teamCount, int[] personWeight, int minSize, int maxSize) {
    if (teamCount < 1) {
      throw new IllegalArgumentException("teamCount must be >= 1");
    }
    if (personWeight == null) {
      throw new IllegalArgumentException("personWeight must not be null");
    }
    if (minSize < 0) {
      throw new IllegalArgumentException("minSize must be >= 0");
    }
    if (maxSize < minSize) {
      throw new IllegalArgumentException("maxSize must be >= minSize");
    }
    for (int weight : personWeight) {
      if (weight < 0) {
        throw new IllegalArgumentException("personWeight entries must be >= 0");
      }
    }
    this.teamCount = teamCount;
    this.personWeight = personWeight.clone();
    this.minSize = minSize;
    this.maxSize = maxSize;
  }

  /** Returns the number of people to partition. */
  public int personCount() {
    return personWeight.length;
  }

  /** Returns the number of teams. */
  public int teamCount() {
    return teamCount;
  }

  @Override
  public int geneCount() {
    return personWeight.length;
  }

  @Override
  public int optionCount(int gene) {
    return teamCount;
  }

  @Override
  public double penalty(int[] genes) {
    int[] size = new int[teamCount];
    long[] weight = new long[teamCount];
    long totalWeight = 0;
    for (int person = 0; person < genes.length; person++) {
      int team = genes[person];
      size[team]++;
      weight[team] += personWeight[person];
      totalWeight += personWeight[person];
    }

    double averageWeight = (double) totalWeight / teamCount;
    double penalty = 0.0;
    for (int team = 0; team < teamCount; team++) {
      int over = size[team] - maxSize;
      if (over > 0) {
        penalty += (double) over * over;
      }
      int under = minSize - size[team];
      if (under > 0) {
        penalty += (double) under * under;
      }
      double deviation = weight[team] - averageWeight;
      penalty += deviation * deviation;
    }
    return penalty;
  }
}
