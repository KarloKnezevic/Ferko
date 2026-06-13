package hr.fer.zemris.ferko.scheduling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Assignment of unscheduled students to lecture groups (Marko Čupić's thesis, section 4.2).
 *
 * <p>Each gene represents one (student, course) enrolment slot, and its value is the index of the
 * chosen lecture group for that course. {@code optionCount(g)} equals the number of groups offered
 * for the course backing gene {@code g}. Two genes are treated as belonging to the same course (and
 * therefore competing for the same group capacities) when they share identical option arrays, i.e.
 * the same {@code groupCapacity[g]} and {@code groupSlot[g]} vectors.
 *
 * <p>The penalty combines two terms, mirroring the thesis:
 *
 * <ul>
 *   <li><b>Over-capacity</b> (soft): for every (course, option) the number of genes assigned to it
 *       beyond the option capacity is penalised non-linearly as {@code max(0, assigned -
 *       capacity)^alpha} with {@code alpha = 2} by default.
 *   <li><b>Overlap</b> (hard): a single student must not attend two groups scheduled in the same
 *       time slot. For each student the number of such conflicting gene pairs is counted, and the
 *       per-student count is penalised as {@code perStudentOverlap^beta} with {@code beta = 2} by
 *       default. A large multiplier makes any overlap dominate so that a penalty of {@code 0} means
 *       no student has a time clash (all hard constraints satisfied).
 * </ul>
 *
 * <p>The class is immutable: all array inputs are defensively deep-copied and never exposed.
 */
public final class UnscheduledStudentsProblem implements Problem {

  /** Multiplier making any time-overlap (hard) violation dominate soft over-capacity penalties. */
  private static final double HARD_OVERLAP_WEIGHT = 1_000_000.0;

  private final int[][] groupCapacity;
  private final int[][] groupSlot;
  private final int[] studentOfGene;
  private final double alpha;
  private final double beta;

  /**
   * Creates the problem.
   *
   * @param groupCapacity per gene, the capacity of each selectable group option
   * @param groupSlot per gene, the time-slot identifier of each selectable group option
   * @param studentOfGene per gene, the owning student index
   * @param alpha over-capacity exponent ({@code >= 1})
   * @param beta per-student overlap exponent ({@code >= 1})
   * @throws IllegalArgumentException if any input is null, inconsistent, or out of range
   */
  public UnscheduledStudentsProblem(
      int[][] groupCapacity, int[][] groupSlot, int[] studentOfGene, double alpha, double beta) {
    if (groupCapacity == null || groupSlot == null || studentOfGene == null) {
      throw new IllegalArgumentException("inputs must not be null");
    }
    int genes = groupCapacity.length;
    if (groupSlot.length != genes || studentOfGene.length != genes) {
      throw new IllegalArgumentException(
          "groupCapacity, groupSlot and studentOfGene length differ");
    }
    if (alpha < 1.0 || beta < 1.0) {
      throw new IllegalArgumentException("alpha and beta must be >= 1");
    }
    for (int g = 0; g < genes; g++) {
      if (groupCapacity[g] == null || groupSlot[g] == null) {
        throw new IllegalArgumentException("option rows must not be null");
      }
      if (groupCapacity[g].length == 0) {
        throw new IllegalArgumentException("each gene needs at least one group option");
      }
      if (groupCapacity[g].length != groupSlot[g].length) {
        throw new IllegalArgumentException("capacity and slot option counts differ for gene " + g);
      }
      for (int c : groupCapacity[g]) {
        if (c < 0) {
          throw new IllegalArgumentException("capacities must be >= 0");
        }
      }
      if (studentOfGene[g] < 0) {
        throw new IllegalArgumentException("student indices must be >= 0");
      }
    }
    this.groupCapacity = deepCopy(groupCapacity);
    this.groupSlot = deepCopy(groupSlot);
    this.studentOfGene = studentOfGene.clone();
    this.alpha = alpha;
    this.beta = beta;
  }

  /** Number of distinct students referenced by the genes. */
  public int studentCount() {
    int max = -1;
    for (int s : studentOfGene) {
      if (s > max) {
        max = s;
      }
    }
    return max + 1;
  }

  @Override
  public int geneCount() {
    return groupCapacity.length;
  }

  @Override
  public int optionCount(int gene) {
    return groupCapacity[gene].length;
  }

  @Override
  public double penalty(int[] genes) {
    if (genes == null || genes.length != groupCapacity.length) {
      throw new IllegalArgumentException("genes length must equal geneCount()");
    }
    return overCapacityPenalty(genes) + HARD_OVERLAP_WEIGHT * overlapPenalty(genes);
  }

  private double overCapacityPenalty(int[] genes) {
    // Group genes by course (identical option signature), then count assignments per option.
    Map<String, int[]> assignedByCourse = new HashMap<>();
    Map<String, int[]> capacityByCourse = new HashMap<>();
    for (int g = 0; g < genes.length; g++) {
      int option = genes[g];
      if (option < 0 || option >= groupCapacity[g].length) {
        throw new IllegalArgumentException("gene " + g + " option out of range: " + option);
      }
      String course = courseKey(g);
      capacityByCourse.putIfAbsent(course, groupCapacity[g]);
      int[] assigned = assignedByCourse.get(course);
      if (assigned == null) {
        assigned = new int[groupCapacity[g].length];
        assignedByCourse.put(course, assigned);
      }
      assigned[option]++;
    }
    double penalty = 0.0;
    for (Map.Entry<String, int[]> entry : assignedByCourse.entrySet()) {
      int[] assigned = entry.getValue();
      int[] capacity = capacityByCourse.get(entry.getKey());
      for (int option = 0; option < assigned.length; option++) {
        int overflow = assigned[option] - capacity[option];
        if (overflow > 0) {
          penalty += Math.pow(overflow, alpha);
        }
      }
    }
    return penalty;
  }

  private double overlapPenalty(int[] genes) {
    int students = studentCount();
    double penalty = 0.0;
    for (int student = 0; student < students; student++) {
      int overlaps = 0;
      for (int i = 0; i < genes.length; i++) {
        if (studentOfGene[i] != student) {
          continue;
        }
        for (int j = i + 1; j < genes.length; j++) {
          if (studentOfGene[j] != student) {
            continue;
          }
          if (groupSlot[i][genes[i]] == groupSlot[j][genes[j]]) {
            overlaps++;
          }
        }
      }
      if (overlaps > 0) {
        penalty += Math.pow(overlaps, beta);
      }
    }
    return penalty;
  }

  private String courseKey(int gene) {
    return Arrays.toString(groupCapacity[gene]) + "|" + Arrays.toString(groupSlot[gene]);
  }

  private static int[][] deepCopy(int[][] source) {
    int[][] copy = new int[source.length][];
    for (int i = 0; i < source.length; i++) {
      copy[i] = source[i].clone();
    }
    return copy;
  }
}
