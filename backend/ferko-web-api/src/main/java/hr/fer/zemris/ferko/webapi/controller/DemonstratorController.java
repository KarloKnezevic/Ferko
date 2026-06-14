package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.audit.AuditService;
import hr.fer.zemris.ferko.application.usecase.demonstrator.DemonstratorService;
import hr.fer.zemris.ferko.application.usecase.demonstrator.DemonstratorViews.DemonstratorView;
import hr.fer.zemris.ferko.application.usecase.demonstrator.DemonstratorViews.MyDemonstratorDutyView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Course demonstrators ("demonstrature"). Listing is visible to anyone related to the course;
 * assignment and removal are course-holder/organiser duties; demonstrators see their own duties.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class DemonstratorController {

  private static final String CAN_MANAGE =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'ASISTENT_ORGANIZATOR')";

  private final DemonstratorService demonstrators;
  private final CourseAccessGuard courseAccess;
  private final AuditService audit;

  public DemonstratorController(
      DemonstratorService demonstrators, CourseAccessGuard courseAccess, AuditService audit) {
    this.demonstrators = demonstrators;
    this.courseAccess = courseAccess;
    this.audit = audit;
  }

  @GetMapping("/courses/{courseId}/demonstrators")
  public List<DemonstratorView> list(@PathVariable long courseId, Authentication authentication) {
    courseAccess.requireCourseAccess(authentication, courseId);
    return demonstrators.listForCourse(courseId);
  }

  @PostMapping("/courses/{courseId}/demonstrators")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assign(
      @PathVariable long courseId,
      @Valid @RequestBody AssignDemonstratorRequest request,
      Authentication authentication) {
    courseAccess.requireCourseAccess(authentication, courseId);
    if (!demonstrators.assignByJmbag(courseId, request.jmbag())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student ne postoji.");
    }
    audit.record(
        authentication.getName(),
        "DEMONSTRATOR_ASSIGNED",
        "course",
        String.valueOf(courseId),
        "jmbag=" + request.jmbag());
  }

  @DeleteMapping("/courses/{courseId}/demonstrators/{studentId}")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(
      @PathVariable long courseId, @PathVariable long studentId, Authentication authentication) {
    courseAccess.requireCourseAccess(authentication, courseId);
    if (!demonstrators.remove(courseId, studentId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Demonstrator ne postoji.");
    }
    audit.record(
        authentication.getName(),
        "DEMONSTRATOR_REMOVED",
        "course",
        String.valueOf(courseId),
        "student=" + studentId);
  }

  @GetMapping("/my/demonstratures")
  public List<MyDemonstratorDutyView> myDuties(Authentication authentication) {
    return demonstrators.myDuties(authentication.getName());
  }

  /** Request to assign a demonstrator by JMBAG. */
  public record AssignDemonstratorRequest(@NotBlank String jmbag) {}
}
