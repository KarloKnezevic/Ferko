package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.calendar.CalendarService;
import hr.fer.zemris.ferko.application.usecase.calendar.CalendarView;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The current user's calendar: weekly teaching timetable plus dated assessments, aggregated across
 * their courses.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class CalendarController {

  private final CalendarService calendarService;

  public CalendarController(CalendarService calendarService) {
    this.calendarService = calendarService;
  }

  @GetMapping("/calendar")
  public CalendarView calendar(Authentication authentication) {
    return calendarService.forUser(authentication.getName());
  }
}
