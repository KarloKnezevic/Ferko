package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.exam.ExamSchedulingService;
import hr.fer.zemris.ferko.application.usecase.exam.ExamView;
import hr.fer.zemris.ferko.application.usecase.exam.RoomSeatingView;
import hr.fer.zemris.ferko.application.usecase.exam.SeatingResult;
import hr.fer.zemris.ferko.application.usecase.exam.SeatingStrategy;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Assessment ("provjera znanja") workflow: define exams, reserve rooms, register students and
 * produce the seating with the evolutionary scheduler. Privileged actions require course-managing
 * roles.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class ExamController {

  private static final String CAN_MANAGE =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'ASISTENT_ORGANIZATOR')";

  private final ExamSchedulingService scheduling;

  public ExamController(ExamSchedulingService scheduling) {
    this.scheduling = scheduling;
  }

  @GetMapping("/courses/{courseId}/exams")
  public List<ExamView> listExams(@PathVariable long courseId) {
    return scheduling.listExams(courseId);
  }

  @PostMapping("/courses/{courseId}/exams")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.CREATED)
  public ExamCreatedResponse createExam(
      @PathVariable long courseId, @RequestBody CreateExamRequest request) {
    long id =
        scheduling.createExam(
            courseId,
            request.title(),
            request.shortName(),
            request.kind(),
            request.startsAt(),
            request.durationMinutes(),
            request.maxPoints());
    return new ExamCreatedResponse(id);
  }

  @PostMapping("/exams/{examId}/rooms")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reserveRoom(@PathVariable long examId, @RequestBody ReserveRoomRequest request) {
    scheduling.reserveRoom(
        examId, request.roomId(), request.capacity(), request.requiredAssistants());
  }

  @PostMapping("/exams/{examId}/registrations/from-course/{courseId}")
  @PreAuthorize(CAN_MANAGE)
  public RegistrationResult registerEnrolled(
      @PathVariable long examId, @PathVariable long courseId) {
    return new RegistrationResult(scheduling.registerEnrolledStudents(examId, courseId));
  }

  @PostMapping("/exams/{examId}/seating")
  @PreAuthorize(CAN_MANAGE)
  public SeatingResult generateSeating(
      @PathVariable long examId, @RequestParam(defaultValue = "GENETIC") SeatingStrategy strategy) {
    return scheduling.generateSeating(examId, strategy);
  }

  @GetMapping("/exams/{examId}/seating")
  public List<RoomSeatingView> seating(@PathVariable long examId) {
    return scheduling.roomSeating(examId);
  }

  @PostMapping("/exams/{examId}/publish")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void publish(@PathVariable long examId) {
    scheduling.publish(examId);
  }

  /** Request to define a new assessment. */
  public record CreateExamRequest(
      @NotBlank String title,
      @NotBlank String shortName,
      @NotBlank String kind,
      LocalDateTime startsAt,
      int durationMinutes,
      double maxPoints) {}

  /** Result of creating an assessment. */
  public record ExamCreatedResponse(long id) {}

  /** Request to reserve a room for an assessment. */
  public record ReserveRoomRequest(long roomId, int capacity, int requiredAssistants) {}

  /** Result of bulk-registering enrolled students. */
  public record RegistrationResult(int registered) {}
}
