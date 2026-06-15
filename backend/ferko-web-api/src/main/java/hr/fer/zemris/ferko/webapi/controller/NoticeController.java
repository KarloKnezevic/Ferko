package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.notice.NoticeService;
import hr.fer.zemris.ferko.application.usecase.notice.NoticeView;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Portal announcements ("obavijesti"): faculty-wide and per-course. Reading is open to any
 * authenticated user; publishing and deleting require a teaching or organising role (deletion is
 * additionally row-level: staff may only delete notices for the courses they teach).
 */
@RestController
@RequestMapping("/api/v1/academic")
public class NoticeController {

  private static final String CAN_PUBLISH =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'STUSLU')";

  private final NoticeService noticeService;

  public NoticeController(NoticeService noticeService) {
    this.noticeService = noticeService;
  }

  @GetMapping("/notices")
  public List<NoticeView> recent(
      @RequestParam(defaultValue = "20") int limit, Authentication authentication) {
    return noticeService.recent(limit, authentication.getName(), rolesOf(authentication));
  }

  @GetMapping("/courses/{courseId}/notices")
  public List<NoticeView> forCourse(@PathVariable long courseId, Authentication authentication) {
    return noticeService.forCourse(courseId, authentication.getName(), rolesOf(authentication));
  }

  @PostMapping("/notices")
  @PreAuthorize(CAN_PUBLISH)
  @ResponseStatus(HttpStatus.CREATED)
  public PublishedResponse publish(
      @RequestBody PublishNoticeRequest request, Authentication authentication) {
    long id =
        noticeService.publish(
            request.courseId(),
            request.title(),
            request.body(),
            request.pinned(),
            authentication.getName());
    return new PublishedResponse(id);
  }

  @DeleteMapping("/notices/{id}")
  @PreAuthorize(CAN_PUBLISH)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id, Authentication authentication) {
    NoticeService.DeleteOutcome outcome =
        noticeService.delete(id, authentication.getName(), rolesOf(authentication));
    switch (outcome) {
      case NOT_FOUND ->
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Obavijest ne postoji.");
      case FORBIDDEN ->
          throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "Nemate ovlasti za brisanje ove obavijesti.");
      default -> {
        // deleted
      }
    }
  }

  private static Set<String> rolesOf(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> authority.startsWith("ROLE_"))
        .map(authority -> authority.substring("ROLE_".length()))
        .collect(Collectors.toSet());
  }

  /** Request to publish a notice; {@code courseId} null means a faculty-wide notice. */
  public record PublishNoticeRequest(Long courseId, String title, String body, boolean pinned) {}

  public record PublishedResponse(long id) {}
}
