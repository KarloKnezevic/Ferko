package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.consultation.ConsultationService;
import hr.fer.zemris.ferko.application.usecase.consultation.ConsultationView;
import hr.fer.zemris.ferko.webapi.auth.FerkoPrincipal;
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
 * Office hours ("konzultacije") for a course: reading is open to anyone authenticated; teaching
 * staff publish and remove their slots.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class ConsultationController {

  private static final String CAN_MANAGE =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT')";

  private final ConsultationService consultationService;

  public ConsultationController(ConsultationService consultationService) {
    this.consultationService = consultationService;
  }

  @GetMapping("/courses/{courseId}/consultations")
  public List<ConsultationView> list(@PathVariable long courseId) {
    return consultationService.forCourse(courseId);
  }

  @PostMapping("/courses/{courseId}/consultations")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.CREATED)
  public CreatedResponse add(
      @PathVariable long courseId,
      @RequestBody CreateConsultationRequest request,
      Authentication authentication) {
    try {
      return new CreatedResponse(
          consultationService.add(
              courseId,
              staffName(authentication),
              request.dayOfWeek(),
              request.startsAt(),
              request.endsAt(),
              request.location()));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  @DeleteMapping("/courses/{courseId}/consultations/{consultationId}")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(@PathVariable long courseId, @PathVariable long consultationId) {
    consultationService.remove(courseId, consultationId);
  }

  private static String staffName(Authentication authentication) {
    if (authentication.getPrincipal() instanceof FerkoPrincipal principal) {
      return principal.fullName();
    }
    return authentication.getName();
  }

  public record CreateConsultationRequest(
      String dayOfWeek, String startsAt, String endsAt, String location) {}

  public record CreatedResponse(long id) {}
}
