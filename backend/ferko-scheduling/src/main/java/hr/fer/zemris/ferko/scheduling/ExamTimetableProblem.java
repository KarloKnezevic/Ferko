package hr.fer.zemris.ferko.scheduling;

/**
 * Assignment of exams to time slots minimising student conflicts. Gene {@code i} = time-slot index
 * for exam {@code i}; penalty is the total number of (student, exam-pair) conflicts produced by
 * placing two exams that share students into the same slot. A penalty of 0 means no student has two
 * exams scheduled in the same slot — Čupić's "raspored obaveznih provjera znanja" objective.
 */
public final class ExamTimetableProblem implements Problem {

  private final int examCount;
  private final int timeSlotCount;
  private final int[][] sharedStudents;

  public ExamTimetableProblem(int examCount, int timeSlotCount, int[][] sharedStudents) {
    if (examCount < 0) {
      throw new IllegalArgumentException("examCount must be >= 0");
    }
    if (timeSlotCount < 1) {
      throw new IllegalArgumentException("timeSlotCount must be >= 1");
    }
    if (sharedStudents == null || sharedStudents.length != examCount) {
      throw new IllegalArgumentException("sharedStudents must be examCount x examCount");
    }
    this.examCount = examCount;
    this.timeSlotCount = timeSlotCount;
    this.sharedStudents = deepCopy(sharedStudents);
  }

  @Override
  public int geneCount() {
    return examCount;
  }

  @Override
  public int optionCount(int gene) {
    return timeSlotCount;
  }

  @Override
  public double penalty(int[] genes) {
    long conflicts = 0;
    for (int i = 0; i < examCount; i++) {
      for (int j = i + 1; j < examCount; j++) {
        if (genes[i] == genes[j]) {
          conflicts += sharedStudents[i][j];
        }
      }
    }
    return conflicts;
  }

  private static int[][] deepCopy(int[][] source) {
    int[][] copy = new int[source.length][];
    for (int i = 0; i < source.length; i++) {
      copy[i] = source[i].clone();
    }
    return copy;
  }
}
