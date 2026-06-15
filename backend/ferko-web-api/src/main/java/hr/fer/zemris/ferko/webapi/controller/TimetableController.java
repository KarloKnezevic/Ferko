package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.audit.AuditService;
import hr.fer.zemris.ferko.application.usecase.timetable.CourseConflictMatrixService;
import hr.fer.zemris.ferko.application.usecase.timetable.CourseConflictMatrixViews.CourseConflictMatrixView;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingService;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.AppliedTimetableView;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.ComparisonView;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.GeneratedTimetableView;
import hr.fer.zemris.ferko.application.usecase.timetable.ScheduleResolutionService;
import hr.fer.zemris.ferko.application.usecase.timetable.ScheduleResolutionViews.ResolutionReportView;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableService;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.CollisionReportView;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.TimetableSlotView;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The faculty-wide weekly teaching timetable. The full grid is readable by any authenticated user
 * (students browse it to plan); the collision report is administrative.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class TimetableController {

  private final TimetableService timetable;
  private final LectureTimetablingService lectureTimetabling;
  private final ScheduleResolutionService resolution;
  private final CourseConflictMatrixService conflictMatrix;
  private final AuditService audit;

  public TimetableController(
      TimetableService timetable,
      LectureTimetablingService lectureTimetabling,
      ScheduleResolutionService resolution,
      CourseConflictMatrixService conflictMatrix,
      AuditService audit) {
    this.timetable = timetable;
    this.lectureTimetabling = lectureTimetabling;
    this.resolution = resolution;
    this.conflictMatrix = conflictMatrix;
    this.audit = audit;
  }

  @GetMapping("/timetable")
  public List<TimetableSlotView> weekly() {
    return timetable.weekly();
  }

  @GetMapping("/timetable/collisions")
  @PreAuthorize("hasRole('ADMIN')")
  public CollisionReportView collisions() {
    return timetable.collisions();
  }

  @PostMapping("/timetable/generate")
  @PreAuthorize("hasRole('ADMIN')")
  public GeneratedTimetableView generate(@RequestBody GenerateRequest request) {
    List<Long> courseIds = request.courseIds();
    if ((courseIds == null || courseIds.isEmpty()) && request.studyYear() != null) {
      courseIds = lectureTimetabling.coursesForStudyYear(request.studyYear());
    }
    int periods = request.periods() == null || request.periods() < 1 ? 15 : request.periods();
    return lectureTimetabling.generate(
        courseIds == null ? List.of() : courseIds, periods, request.algorithm());
  }

  @PostMapping("/timetable/compare")
  @PreAuthorize("hasRole('ADMIN')")
  public ComparisonView compare(@RequestBody GenerateRequest request) {
    List<Long> courseIds = request.courseIds();
    if ((courseIds == null || courseIds.isEmpty()) && request.studyYear() != null) {
      courseIds = lectureTimetabling.coursesForStudyYear(request.studyYear());
    }
    int periods = request.periods() == null || request.periods() < 1 ? 15 : request.periods();
    return lectureTimetabling.compare(courseIds == null ? List.of() : courseIds, periods);
  }

  @PostMapping("/timetable/apply")
  @PreAuthorize("hasRole('ADMIN')")
  @org.springframework.transaction.annotation.Transactional
  public AppliedTimetableView apply(
      @RequestBody GenerateRequest request, Authentication authentication) {
    List<Long> courseIds = request.courseIds();
    if ((courseIds == null || courseIds.isEmpty()) && request.studyYear() != null) {
      courseIds = lectureTimetabling.coursesForStudyYear(request.studyYear());
    }
    int periods = request.periods() == null || request.periods() < 1 ? 15 : request.periods();
    AppliedTimetableView result =
        lectureTimetabling.apply(
            courseIds == null ? List.of() : courseIds, periods, request.algorithm());
    audit.record(
        authentication.getName(),
        "TIMETABLE_APPLIED",
        "timetable",
        null,
        result.courses() + " courses, " + result.slotsWritten() + " slots, " + result.algorithm());
    return result;
  }

  @GetMapping("/timetable/resolution")
  @PreAuthorize("hasRole('ADMIN')")
  public ResolutionReportView resolutionReport() {
    return resolution.report();
  }

  /**
   * Course-overlap conflict matrix (shared students between course pairs) for a semester, or the
   * active semester when {@code semester} is omitted. Drives the admin overlap heatmap.
   */
  @GetMapping("/timetable/conflict-matrix")
  @PreAuthorize("hasRole('ADMIN')")
  public CourseConflictMatrixView conflictMatrix(
      @org.springframework.web.bind.annotation.RequestParam(value = "semester", required = false)
          String semester) {
    return conflictMatrix.matrix(semester);
  }

  /** Ranked free slots ("gaps") a colliding session can be moved into, best-first by soft score. */
  @GetMapping("/timetable/resolution/candidates")
  @PreAuthorize("hasRole('ADMIN')")
  public java.util.List<
          hr.fer.zemris.ferko.application.usecase.timetable.ScheduleResolutionViews.CandidateView>
      candidates(
          @org.springframework.web.bind.annotation.RequestParam long slotId,
          @org.springframework.web.bind.annotation.RequestParam(defaultValue = "8") int limit) {
    return resolution.candidates(slotId, Math.min(Math.max(limit, 1), 25));
  }

  /** Drag-and-drop board: rooms and the weekly sessions of one room (with slot ids to move). */
  @GetMapping("/timetable/resolution/board")
  @PreAuthorize("hasRole('ADMIN')")
  public hr.fer.zemris.ferko.application.usecase.timetable.ScheduleResolutionViews.BoardView board(
      @org.springframework.web.bind.annotation.RequestParam(value = "roomId", required = false)
          Long roomId) {
    return resolution.board(roomId);
  }

  @PostMapping("/timetable/resolution/move")
  @PreAuthorize("hasRole('ADMIN')")
  @org.springframework.transaction.annotation.Transactional
  public ResolutionReportView move(
      @RequestBody MoveRequest request, Authentication authentication) {
    ResolutionReportView report =
        resolution.move(
            request.slotId(), request.dayOfWeek(), request.startsAt(), request.roomId());
    audit.record(
        authentication.getName(),
        "TIMETABLE_SLOT_MOVED",
        "class_schedule",
        String.valueOf(request.slotId()),
        request.dayOfWeek() + " " + request.startsAt() + " room " + request.roomId());
    return report;
  }

  @PostMapping("/timetable/resolution/auto")
  @PreAuthorize("hasRole('ADMIN')")
  @org.springframework.transaction.annotation.Transactional
  public ResolutionReportView autoResolve(Authentication authentication) {
    ResolutionReportView report = resolution.autoResolve();
    audit.record(
        authentication.getName(),
        "TIMETABLE_AUTO_RESOLVED",
        "timetable",
        null,
        "conflictFree=" + report.conflictFree());
    return report;
  }

  @PostMapping("/timetable/resolution/generate")
  @PreAuthorize("hasRole('ADMIN')")
  @org.springframework.transaction.annotation.Transactional
  public ResolutionReportView generateFacultyWide(Authentication authentication) {
    ResolutionReportView report = resolution.generateFacultyWide();
    audit.record(
        authentication.getName(),
        "TIMETABLE_FACULTY_GENERATED",
        "timetable",
        null,
        "slots=" + report.totalSlots() + ", conflictFree=" + report.conflictFree());
    return report;
  }

  /**
   * Generation request: scope is either an explicit {@code courseIds} list or a {@code studyYear};
   * {@code periods} weekly slots (default 15 = 5 days x 3 blocks); {@code algorithm} optional.
   */
  public record GenerateRequest(
      List<Long> courseIds, Integer studyYear, Integer periods, String algorithm) {}

  /** Move a single timetable slot to a new weekday/time/room (duration preserved). */
  public record MoveRequest(long slotId, String dayOfWeek, String startsAt, Long roomId) {}
}
