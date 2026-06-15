package hr.fer.zemris.ferko.webapi.bootstrap;

import hr.fer.zemris.ferko.application.usecase.academic.AcademicQueryService;
import hr.fer.zemris.ferko.application.usecase.academic.CourseSummaryView;
import hr.fer.zemris.ferko.application.usecase.academic.EnrollmentView;
import hr.fer.zemris.ferko.application.usecase.academic.SemesterView;
import hr.fer.zemris.ferko.application.usecase.grading.GradeComponentView;
import hr.fer.zemris.ferko.application.usecase.grading.GradeScale;
import hr.fer.zemris.ferko.application.usecase.grading.GradingService;
import hr.fer.zemris.ferko.webapi.config.FerkoProperties;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Simulates a fully graded active semester: for every seeded course it creates grade components
 * (midterm, final, lab), generates deterministic per-student points, and records a final grade 1–5
 * via the configured {@link GradeScale}. Runs after {@link AcademicDataSeeder} (Order 7) and is
 * idempotent — a course whose components already exist is skipped.
 */
@Component
@Order(7)
@ConditionalOnProperty(
    name = "ferko.seed.academic.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class GradeSeeder implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(GradeSeeder.class);
  private static final String DECIDED_BY = "system";

  /** Weighted components summing to 100 points. */
  private static final List<Component3> COMPONENTS =
      List.of(
          new Component3("Međuispit", "MI", 30),
          new Component3("Završni ispit", "ZI", 40),
          new Component3("Laboratorijske vježbe", "LAB", 30));

  private final AcademicQueryService query;
  private final GradingService grading;
  private final GradeScale scale;

  public GradeSeeder(
      AcademicQueryService query, GradingService grading, FerkoProperties properties) {
    this.query = query;
    this.grading = grading;
    FerkoProperties.Grading thresholds = properties.getGrading();
    this.scale =
        new GradeScale(
            thresholds.getExcellent(),
            thresholds.getVeryGood(),
            thresholds.getGood(),
            thresholds.getSufficient());
  }

  @Override
  public void run(ApplicationArguments args) {
    String semester = query.activeSemester().map(SemesterView::code).orElse(null);
    if (semester == null) {
      return;
    }
    int gradedCourses = 0;
    for (CourseSummaryView course : query.listCourses(semester)) {
      if (!grading.listComponents(course.id()).isEmpty()) {
        continue; // already graded
      }
      seedCourse(course.id());
      gradedCourses++;
    }
    LOG.info("Grade simulation complete for {} courses.", gradedCourses);
  }

  private void seedCourse(long courseId) {
    long[] componentIds = new long[COMPONENTS.size()];
    double[] maxPoints = new double[COMPONENTS.size()];
    for (int i = 0; i < COMPONENTS.size(); i++) {
      Component3 component = COMPONENTS.get(i);
      GradeComponentView view =
          grading.addComponent(
              courseId, component.name(), component.shortName(), component.maxPoints(), i + 1);
      componentIds[i] = view.id();
      maxPoints[i] = component.maxPoints();
    }

    double maxTotal = 0;
    for (double max : maxPoints) {
      maxTotal += max;
    }

    for (EnrollmentView enrollment : query.listEnrollments(courseId)) {
      // Deterministic per (student, course) so re-seeding a fresh DB reproduces the same grades.
      Random random = new Random((enrollment.studentId() * 1_000_003L) ^ courseId);
      double total = 0;
      for (int i = 0; i < COMPONENTS.size(); i++) {
        double fraction = Math.max(0.0, Math.min(1.0, 0.30 + 0.65 * random.nextDouble()));
        double points = Math.round(maxPoints[i] * fraction * 10.0) / 10.0;
        total += points;
        grading.enterPoints(
            courseId, enrollment.studentId(), componentIds[i], points, maxPoints[i], DECIDED_BY);
      }
      int grade = scale.gradeForScore(total, maxTotal);
      grading.recordGrade(courseId, enrollment.studentId(), grade, total, DECIDED_BY);
    }
  }

  private record Component3(String name, String shortName, double maxPoints) {}
}
