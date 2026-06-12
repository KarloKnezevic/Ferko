package hr.fer.zemris.ferko.scheduling;

/**
 * Assignment of students to exam rooms. Gene {@code i} = room index for student {@code i}; penalty
 * is the non-linearly weighted over-capacity of the rooms ({@code Σ max(0, load -
 * capacity)^alpha}), following Čupić's over-capacity penalty. A penalty of 0 means no room is over
 * capacity, which is always achievable when total capacity covers the cohort.
 */
public final class SeatingProblem implements Problem {

  private final int studentCount;
  private final int[] roomCapacities;
  private final double alpha;

  public SeatingProblem(int studentCount, int[] roomCapacities, double alpha) {
    if (studentCount < 0) {
      throw new IllegalArgumentException("studentCount must be >= 0");
    }
    if (roomCapacities == null || roomCapacities.length == 0) {
      throw new IllegalArgumentException("at least one room is required");
    }
    if (alpha < 1.0) {
      throw new IllegalArgumentException("alpha must be >= 1");
    }
    this.studentCount = studentCount;
    this.roomCapacities = roomCapacities.clone();
    this.alpha = alpha;
  }

  public int roomCount() {
    return roomCapacities.length;
  }

  public int totalCapacity() {
    int total = 0;
    for (int capacity : roomCapacities) {
      total += capacity;
    }
    return total;
  }

  @Override
  public int geneCount() {
    return studentCount;
  }

  @Override
  public int optionCount(int gene) {
    return roomCapacities.length;
  }

  @Override
  public double penalty(int[] genes) {
    int[] load = new int[roomCapacities.length];
    for (int room : genes) {
      load[room]++;
    }
    double penalty = 0.0;
    for (int room = 0; room < load.length; room++) {
      int overflow = load[room] - roomCapacities[room];
      if (overflow > 0) {
        penalty += Math.pow(overflow, alpha);
      }
    }
    return penalty;
  }
}
