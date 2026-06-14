package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.timetable.TimetableService;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.CollisionReportView;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.TimetableSlotView;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

  public TimetableController(TimetableService timetable) {
    this.timetable = timetable;
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
}
