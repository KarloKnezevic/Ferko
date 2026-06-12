package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.academic.AcademicProvisioningService;
import hr.fer.zemris.ferko.application.usecase.academic.AcademicQueryService;
import hr.fer.zemris.ferko.application.usecase.academic.SemesterView;
import hr.fer.zemris.ferko.application.usecase.academic.StudentView;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Administrative write operations over the academic data model. */
@RestController
@RequestMapping("/api/v1/academic")
public class AcademicAdminController {

  private final AcademicProvisioningService provisioning;
  private final AcademicQueryService query;

  public AcademicAdminController(
      AcademicProvisioningService provisioning, AcademicQueryService query) {
    this.provisioning = provisioning;
    this.query = query;
  }

  @PostMapping("/courses")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public CourseCreatedResponse createCourse(@RequestBody CreateCourseRequest request) {
    String semester =
        query
            .activeSemester()
            .map(SemesterView::code)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.CONFLICT, "Nema aktivnog semestra."));
    long id =
        provisioning.provisionCourse(
            request.code(),
            request.name(),
            semester,
            request.ects(),
            request.description(),
            request.literature());
    return new CourseCreatedResponse(id, request.code(), semester);
  }

  @PostMapping("/courses/{courseId}/enrollments")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUSLU')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void enroll(@PathVariable long courseId, @RequestBody EnrollRequest request) {
    long studentId =
        query
            .getStudentByJmbag(request.jmbag())
            .map(StudentView::id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student ne postoji."));
    provisioning.enroll(studentId, courseId, LocalDateTime.now());
  }

  /** Request to create a course in the active semester. */
  public record CreateCourseRequest(
      @NotBlank String code,
      @NotBlank String name,
      int ects,
      String description,
      String literature) {}

  /** Result of creating a course. */
  public record CourseCreatedResponse(long id, String code, String semesterCode) {}

  /** Request to enroll a student (by JMBAG) into a course. */
  public record EnrollRequest(@NotBlank String jmbag) {}
}
