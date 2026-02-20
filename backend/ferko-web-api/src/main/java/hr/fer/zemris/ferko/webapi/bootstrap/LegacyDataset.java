package hr.fer.zemris.ferko.webapi.bootstrap;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public record LegacyDataset(
    Map<String, CourseCatalogEntry> courses,
    List<EnrollmentEntry> enrollments,
    List<ScheduleEntry> schedules,
    List<ExamEntry> exams,
    List<RawLineEntry> rawLines) {

  public record CourseCatalogEntry(
      String courseCode,
      String title,
      String workloadRaw,
      String description,
      String literature,
      String leaders,
      String instructors) {}

  public record EnrollmentEntry(
      String jmbag,
      String courseCode,
      String groupCode,
      String studentName,
      String courseName,
      int yearOfStudy,
      String sourceLine) {}

  public record ScheduleEntry(
      LocalDate date,
      LocalTime startsAt,
      int durationMinutes,
      String institution,
      String room,
      String courseCode,
      String groupsText) {}

  public record ExamEntry(
      String termType,
      LocalDate date,
      LocalTime startsAt,
      int durationHours,
      String courseName,
      String courseCode) {}

  public record RawLineEntry(String sourceFile, int lineNo, String lineText) {}
}
