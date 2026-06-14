package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.schedule.CourseScheduleService;
import hr.fer.zemris.ferko.application.usecase.schedule.ScheduleSlotView;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The weekly teaching timetable ("Raspored nastave") for a course. */
@RestController
@RequestMapping("/api/v1/academic")
public class CourseScheduleController {

  private final CourseScheduleService scheduleService;

  public CourseScheduleController(CourseScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @GetMapping("/courses/{courseId}/schedule")
  public List<ScheduleSlotView> schedule(@PathVariable long courseId) {
    return scheduleService.forCourse(courseId);
  }
}
