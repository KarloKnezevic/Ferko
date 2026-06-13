package hr.fer.zemris.ferko.application.usecase.calendar;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The current user's aggregated calendar: the recurring weekly teaching timetable plus dated
 * assessments, gathered across the courses the user attends (as student) or teaches (as staff).
 */
public record CalendarView(List<WeeklySlot> weekly, List<UpcomingExam> exams) {

  /** One recurring slot in the weekly timetable. */
  public record WeeklySlot(
      String dayOfWeek,
      String startsAt,
      String endsAt,
      String type,
      String courseCode,
      String courseName,
      String room,
      String instructor) {}

  /** One dated assessment. */
  public record UpcomingExam(
      LocalDateTime startsAt,
      String title,
      String shortName,
      String courseCode,
      String courseName,
      int durationMinutes) {}
}
