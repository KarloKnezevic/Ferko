package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Grade;
import hr.fer.zemris.ferko.domain.model.GradeComponent;
import hr.fer.zemris.ferko.domain.model.StudentPoints;
import java.util.List;
import java.util.Optional;

/** Persistence port for grade components, student points and final grades. */
public interface GradingRepository {

  GradeComponent addComponent(GradeComponent component);

  List<GradeComponent> componentsByCourse(long courseId);

  /** Upsert points for a (student, course, component) tuple. */
  StudentPoints savePoints(StudentPoints points);

  List<StudentPoints> pointsByCourse(long courseId);

  Grade saveGrade(Grade grade);

  List<Grade> gradesByCourse(long courseId);

  Optional<Grade> gradeFor(long courseId, long studentId);
}
