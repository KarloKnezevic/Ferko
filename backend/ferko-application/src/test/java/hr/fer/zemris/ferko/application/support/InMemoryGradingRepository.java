package hr.fer.zemris.ferko.application.support;

import hr.fer.zemris.ferko.application.port.GradingRepository;
import hr.fer.zemris.ferko.domain.model.Grade;
import hr.fer.zemris.ferko.domain.model.GradeComponent;
import hr.fer.zemris.ferko.domain.model.StudentPoints;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory fake of {@link GradingRepository} for use-case tests. */
public final class InMemoryGradingRepository implements GradingRepository {

  private final List<GradeComponent> components = new ArrayList<>();
  private final List<StudentPoints> points = new ArrayList<>();
  private final List<Grade> grades = new ArrayList<>();
  private final AtomicLong seq = new AtomicLong(0);

  @Override
  public GradeComponent addComponent(GradeComponent component) {
    GradeComponent stored =
        new GradeComponent(
            seq.incrementAndGet(),
            component.courseId(),
            component.name(),
            component.shortName(),
            component.maxPoints(),
            component.ordinal());
    components.add(stored);
    return stored;
  }

  @Override
  public List<GradeComponent> componentsByCourse(long courseId) {
    return components.stream().filter(c -> c.courseId() == courseId).toList();
  }

  @Override
  public StudentPoints savePoints(StudentPoints p) {
    points.removeIf(
        existing ->
            existing.courseId() == p.courseId()
                && existing.studentId() == p.studentId()
                && Objects.equals(existing.componentId(), p.componentId()));
    StudentPoints stored =
        new StudentPoints(
            seq.incrementAndGet(),
            p.courseId(),
            p.studentId(),
            p.componentId(),
            p.examId(),
            p.points(),
            p.maxPoints(),
            p.published(),
            p.enteredBy(),
            p.enteredAt());
    points.add(stored);
    return stored;
  }

  @Override
  public List<StudentPoints> pointsByCourse(long courseId) {
    return points.stream().filter(p -> p.courseId() == courseId).toList();
  }

  @Override
  public Grade saveGrade(Grade grade) {
    grades.removeIf(g -> g.courseId() == grade.courseId() && g.studentId() == grade.studentId());
    Grade stored =
        new Grade(
            seq.incrementAndGet(),
            grade.courseId(),
            grade.studentId(),
            grade.finalGrade(),
            grade.pointsTotal(),
            grade.decidedBy(),
            grade.decidedAt());
    grades.add(stored);
    return stored;
  }

  @Override
  public List<Grade> gradesByCourse(long courseId) {
    return grades.stream().filter(g -> g.courseId() == courseId).toList();
  }

  @Override
  public Optional<Grade> gradeFor(long courseId, long studentId) {
    return grades.stream()
        .filter(g -> g.courseId() == courseId && g.studentId() == studentId)
        .findFirst();
  }
}
