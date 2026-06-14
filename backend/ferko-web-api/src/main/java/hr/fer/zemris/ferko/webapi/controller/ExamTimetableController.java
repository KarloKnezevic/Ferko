package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.academic.AcademicQueryService;
import hr.fer.zemris.ferko.application.usecase.academic.CourseSummaryView;
import hr.fer.zemris.ferko.application.usecase.timetable.ExamTimetablingService;
import hr.fer.zemris.ferko.application.usecase.timetable.ExamTimetablingViews.GeneratedExamTimetableView;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.ExamEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDatasetLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Faculty-wide exam-timetable generation. Generates a conflict-free exam schedule with the engine
 * and compares it against the historical (legacy) schedule for the chosen exam term.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class ExamTimetableController {

  private static final String DEFAULT_TERM = "ZI";

  private final ExamTimetablingService examTimetabling;
  private final AcademicQueryService query;
  private final LegacyDatasetLoader datasetLoader;

  public ExamTimetableController(
      ExamTimetablingService examTimetabling,
      AcademicQueryService query,
      LegacyDatasetLoader datasetLoader) {
    this.examTimetabling = examTimetabling;
    this.query = query;
    this.datasetLoader = datasetLoader;
  }

  @PostMapping("/exam-timetable/generate")
  @PreAuthorize("hasRole('ADMIN')")
  public GeneratedExamTimetableView generate(@RequestBody ExamGenerateRequest request) {
    List<Long> courseIds = request.courseIds();
    if ((courseIds == null || courseIds.isEmpty()) && request.studyYear() != null) {
      courseIds = examTimetabling.coursesForStudyYear(request.studyYear());
    }
    if (courseIds == null) {
      courseIds = List.of();
    }
    int slots = request.slots() == null || request.slots() < 1 ? 10 : request.slots();
    String term = request.referenceTerm() == null ? DEFAULT_TERM : request.referenceTerm();
    Map<Long, Integer> legacyAssignment = legacyAssignment(courseIds, term);
    return examTimetabling.generate(courseIds, slots, request.algorithm(), legacyAssignment);
  }

  /** Maps the legacy exam dates of the given term onto a per-course slot id for comparison. */
  private Map<Long, Integer> legacyAssignment(List<Long> courseIds, String term) {
    if (courseIds.isEmpty()) {
      return Map.of();
    }
    // Map across all courses (not just the active semester) so courses in scope always resolve.
    Map<String, Long> idByCode = new HashMap<>();
    for (CourseSummaryView course : query.listCourses(null)) {
      idByCode.put(course.code().toUpperCase(Locale.ROOT), course.id());
    }
    String wantedTerm = term.toUpperCase(Locale.ROOT);
    Map<String, Integer> slotByDate = new HashMap<>();
    Map<Long, Integer> assignment = new HashMap<>();
    for (ExamEntry exam : datasetLoader.load().exams()) {
      if (!exam.termType().equalsIgnoreCase(wantedTerm)) {
        continue;
      }
      Long courseId = idByCode.get(exam.courseCode().toUpperCase(Locale.ROOT));
      if (courseId == null || !courseIds.contains(courseId)) {
        continue;
      }
      String dateKey = exam.date() + "T" + exam.startsAt();
      int slot = slotByDate.computeIfAbsent(dateKey, key -> slotByDate.size());
      assignment.put(courseId, slot);
    }
    return assignment;
  }

  /**
   * Request: scope is an explicit {@code courseIds} list or a {@code studyYear}; {@code slots} exam
   * periods; {@code algorithm} optional; {@code referenceTerm} (ZI/MI1/MI2/PZI) for comparison.
   */
  public record ExamGenerateRequest(
      List<Long> courseIds,
      Integer studyYear,
      Integer slots,
      String algorithm,
      String referenceTerm) {}
}
