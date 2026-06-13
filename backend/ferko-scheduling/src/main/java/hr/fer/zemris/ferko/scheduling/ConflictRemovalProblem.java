package hr.fer.zemris.ferko.scheduling;

/**
 * Removal of timetable conflicts at the student level (Čupić, thesis 4.3). Each gene is a (student,
 * course) registration whose value selects one of the available group options for that
 * registration; {@code groupSlot[gene][option]} maps a chosen option to the time slot it occupies.
 * Starting from an existing assignment that contains some student conflicts, the goal is to find a
 * re-assignment that minimises both the number of remaining conflicts and the number of changes
 * made to the original assignment, preferring to change only genes that were involved in a
 * conflict.
 *
 * <p>The penalty is {@code w1 * remainingConflicts + w2 * changesOnNonConflictGenes^delta + w3 *
 * changesOnConflictGenes^epsilon}, where a remaining conflict is any pair of genes belonging to the
 * same student that resolve to the same time slot. The conflict weight {@code w1} dominates so that
 * removing conflicts (a hard objective) always outranks the soft minimisation of changes. With
 * {@code delta = epsilon = 2} the change penalties grow non-linearly, discouraging many edits. A
 * penalty of 0 means the assignment is conflict-free and identical to the initial assignment.
 */
public final class ConflictRemovalProblem implements Problem {

  private static final int DELTA = 2;
  private static final int EPSILON = 2;
  private static final double DEFAULT_W1 = 1000.0;
  private static final double DEFAULT_W2 = 1.0;
  private static final double DEFAULT_W3 = 1.0;

  private final int[][] groupSlot;
  private final int[] studentOfGene;
  private final int[] initialAssignment;
  private final boolean[] geneHadConflict;
  private final double w1;
  private final double w2;
  private final double w3;

  /**
   * Creates a conflict-removal problem with the default Čupić weights ({@code w1 = 1000}, {@code w2
   * = 1}, {@code w3 = 1}).
   *
   * @param groupSlot per-gene array mapping each group option to its time slot
   * @param studentOfGene student owning each gene
   * @param initialAssignment originally chosen option index per gene
   * @param geneHadConflict whether each gene was involved in a conflict initially
   */
  public ConflictRemovalProblem(
      int[][] groupSlot, int[] studentOfGene, int[] initialAssignment, boolean[] geneHadConflict) {
    this(
        groupSlot,
        studentOfGene,
        initialAssignment,
        geneHadConflict,
        DEFAULT_W1,
        DEFAULT_W2,
        DEFAULT_W3);
  }

  /**
   * Creates a conflict-removal problem with explicit weights.
   *
   * @param groupSlot per-gene array mapping each group option to its time slot
   * @param studentOfGene student owning each gene
   * @param initialAssignment originally chosen option index per gene
   * @param geneHadConflict whether each gene was involved in a conflict initially
   * @param w1 dominating weight on remaining conflicts
   * @param w2 weight on changes to genes that were not in a conflict
   * @param w3 weight on changes to genes that were in a conflict
   */
  public ConflictRemovalProblem(
      int[][] groupSlot,
      int[] studentOfGene,
      int[] initialAssignment,
      boolean[] geneHadConflict,
      double w1,
      double w2,
      double w3) {
    if (groupSlot == null || groupSlot.length == 0) {
      throw new IllegalArgumentException("at least one gene is required");
    }
    int geneCount = groupSlot.length;
    if (studentOfGene == null || studentOfGene.length != geneCount) {
      throw new IllegalArgumentException("studentOfGene must have one entry per gene");
    }
    if (initialAssignment == null || initialAssignment.length != geneCount) {
      throw new IllegalArgumentException("initialAssignment must have one entry per gene");
    }
    if (geneHadConflict == null || geneHadConflict.length != geneCount) {
      throw new IllegalArgumentException("geneHadConflict must have one entry per gene");
    }
    if (w1 < 0 || w2 < 0 || w3 < 0) {
      throw new IllegalArgumentException("weights must be >= 0");
    }
    for (int gene = 0; gene < geneCount; gene++) {
      int[] options = groupSlot[gene];
      if (options == null || options.length == 0) {
        throw new IllegalArgumentException("gene " + gene + " must have at least one option");
      }
      for (int slot : options) {
        if (slot < 0) {
          throw new IllegalArgumentException("slot indices must be >= 0");
        }
      }
      if (studentOfGene[gene] < 0) {
        throw new IllegalArgumentException("student indices must be >= 0");
      }
      if (initialAssignment[gene] < 0 || initialAssignment[gene] >= options.length) {
        throw new IllegalArgumentException("initialAssignment[" + gene + "] out of range");
      }
    }
    this.groupSlot = deepCopy(groupSlot);
    this.studentOfGene = studentOfGene.clone();
    this.initialAssignment = initialAssignment.clone();
    this.geneHadConflict = geneHadConflict.clone();
    this.w1 = w1;
    this.w2 = w2;
    this.w3 = w3;
  }

  @Override
  public int geneCount() {
    return groupSlot.length;
  }

  @Override
  public int optionCount(int gene) {
    return groupSlot[gene].length;
  }

  @Override
  public double penalty(int[] genes) {
    if (genes == null || genes.length != groupSlot.length) {
      throw new IllegalArgumentException("genes must have one value per gene");
    }
    int geneCount = groupSlot.length;
    int remainingConflicts = 0;
    for (int i = 0; i < geneCount; i++) {
      int slotI = groupSlot[i][genes[i]];
      for (int j = i + 1; j < geneCount; j++) {
        if (studentOfGene[i] == studentOfGene[j] && slotI == groupSlot[j][genes[j]]) {
          remainingConflicts++;
        }
      }
    }
    int changesOnNonConflictGenes = 0;
    int changesOnConflictGenes = 0;
    for (int gene = 0; gene < geneCount; gene++) {
      if (genes[gene] != initialAssignment[gene]) {
        if (geneHadConflict[gene]) {
          changesOnConflictGenes++;
        } else {
          changesOnNonConflictGenes++;
        }
      }
    }
    return w1 * remainingConflicts
        + w2 * Math.pow(changesOnNonConflictGenes, DELTA)
        + w3 * Math.pow(changesOnConflictGenes, EPSILON);
  }

  /** Returns the number of distinct students referenced by the genes. */
  public int studentCount() {
    int max = -1;
    for (int student : studentOfGene) {
      if (student > max) {
        max = student;
      }
    }
    return max + 1;
  }

  private static int[][] deepCopy(int[][] source) {
    int[][] copy = new int[source.length][];
    for (int i = 0; i < source.length; i++) {
      copy[i] = source[i].clone();
    }
    return copy;
  }
}
