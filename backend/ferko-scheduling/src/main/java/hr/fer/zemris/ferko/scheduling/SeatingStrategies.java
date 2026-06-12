package hr.fer.zemris.ferko.scheduling;

import java.util.Random;

/**
 * Deterministic seat-filling strategies mirroring the four FERKO buttons: sorted/random students ×
 * greedy/proportional room filling. Each returns a room index per student ({@code result[i]} = room
 * for student {@code i} in the original order).
 */
public final class SeatingStrategies {

  private SeatingStrategies() {}

  /** Sorted students, greedy fill: fill the first room to capacity, then the next, and so on. */
  public static int[] sortedGreedy(int studentCount, int[] roomCapacities) {
    return greedy(naturalOrder(studentCount), roomCapacities);
  }

  /** Sorted students, proportional fill: balance load across rooms by remaining capacity. */
  public static int[] sortedProportional(int studentCount, int[] roomCapacities) {
    return proportional(naturalOrder(studentCount), roomCapacities);
  }

  /** Random (seeded) students, greedy fill. */
  public static int[] randomGreedy(int studentCount, int[] roomCapacities, long seed) {
    return greedy(shuffledOrder(studentCount, seed), roomCapacities);
  }

  /** Random (seeded) students, proportional fill. */
  public static int[] randomProportional(int studentCount, int[] roomCapacities, long seed) {
    return proportional(shuffledOrder(studentCount, seed), roomCapacities);
  }

  private static int[] greedy(int[] order, int[] roomCapacities) {
    int[] assignment = new int[order.length];
    int room = 0;
    int used = 0;
    for (int student : order) {
      while (room < roomCapacities.length - 1 && used >= roomCapacities[room]) {
        room++;
        used = 0;
      }
      assignment[student] = room;
      used++;
    }
    return assignment;
  }

  private static int[] proportional(int[] order, int[] roomCapacities) {
    int[] remaining = roomCapacities.clone();
    int[] assignment = new int[order.length];
    for (int student : order) {
      int chosen = 0;
      for (int room = 1; room < remaining.length; room++) {
        if (remaining[room] > remaining[chosen]) {
          chosen = room;
        }
      }
      assignment[student] = chosen;
      remaining[chosen]--;
    }
    return assignment;
  }

  private static int[] naturalOrder(int count) {
    int[] order = new int[count];
    for (int i = 0; i < count; i++) {
      order[i] = i;
    }
    return order;
  }

  private static int[] shuffledOrder(int count, long seed) {
    int[] order = naturalOrder(count);
    Random rng = new Random(seed);
    for (int i = count - 1; i > 0; i--) {
      int j = rng.nextInt(i + 1);
      int tmp = order[i];
      order[i] = order[j];
      order[j] = tmp;
    }
    return order;
  }
}
