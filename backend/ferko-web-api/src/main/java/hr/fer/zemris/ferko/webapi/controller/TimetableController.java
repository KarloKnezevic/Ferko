package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingService;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.GeneratedTimetableView;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableService;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.CollisionReportView;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.TimetableSlotView;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
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

  public TimetableController(
      TimetableService timetable, LectureTimetablingService lectureTimetabling) {
    this.timetable = timetable;
    this.lectureTimetabling = lectureTimetabling;
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

  /**
   * Generation request: scope is either an explicit {@code courseIds} list or a {@code studyYear};
   * {@code periods} weekly slots (default 15 = 5 days x 3 blocks); {@code algorithm} optional.
   */
  public record GenerateRequest(
      List<Long> courseIds, Integer studyYear, Integer periods, String algorithm) {}
}
