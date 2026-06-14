package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.grading.GradeComponentView;
import hr.fer.zemris.ferko.application.usecase.grading.GradeView;
import hr.fer.zemris.ferko.application.usecase.grading.GradingService;
import hr.fer.zemris.ferko.application.usecase.grading.PointsOverviewRow;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Grading: components, points entry, the points overview ("preglednik bodova") and final grades.
 */
@RestController
@RequestMapping("/api/v1/academic/courses/{courseId}")
public class GradingController {

  private static final String CAN_MANAGE =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT')";

  private final GradingService grading;

  public GradingController(GradingService grading) {
    this.grading = grading;
  }

  @GetMapping("/grade-components")
  public List<GradeComponentView> components(@PathVariable long courseId) {
    return grading.listComponents(courseId);
  }

  @PostMapping("/grade-components")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.CREATED)
  public GradeComponentView addComponent(
      @PathVariable long courseId, @RequestBody CreateComponentRequest request) {
    return grading.addComponent(
        courseId, request.name(), request.shortName(), request.maxPoints(), request.ordinal());
  }

  @PostMapping("/points")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void enterPoints(
      @PathVariable long courseId, @RequestBody EnterPointsRequest request, Principal principal) {
    grading.enterPoints(
        courseId,
        request.studentId(),
        request.componentId(),
        request.points(),
        principalName(principal));
  }

  @GetMapping("/points-overview")
  @PreAuthorize(CAN_MANAGE)
  public List<PointsOverviewRow> pointsOverview(@PathVariable long courseId) {
    return grading.pointsOverview(courseId);
  }

  @PostMapping("/grades")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assignGrade(
      @PathVariable long courseId, @RequestBody AssignGradeRequest request, Principal principal) {
    grading.assignGrade(
        courseId, request.studentId(), request.finalGrade(), principalName(principal));
  }

  @GetMapping("/grades")
  @PreAuthorize(CAN_MANAGE)
  public List<GradeView> grades(@PathVariable long courseId) {
    return grading.listGrades(courseId);
  }

  private static String principalName(Principal principal) {
    return principal == null ? "system" : principal.getName();
  }

  /** Request to define a grade component. */
  public record CreateComponentRequest(
      @NotBlank String name, @NotBlank String shortName, double maxPoints, int ordinal) {}

  /** Request to record points for a student on a component. */
  public record EnterPointsRequest(long studentId, long componentId, double points) {}

  /** Request to assign a final grade. */
  public record AssignGradeRequest(long studentId, int finalGrade) {}
}
