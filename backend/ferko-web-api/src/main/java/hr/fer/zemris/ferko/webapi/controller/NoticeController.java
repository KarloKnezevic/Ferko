package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.notice.NoticeService;
import hr.fer.zemris.ferko.application.usecase.notice.NoticeView;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Portal announcements ("obavijesti"): faculty-wide and per-course. Reading is open to any
 * authenticated user; publishing requires a teaching or organising role.
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
  public List<NoticeView> recent(@RequestParam(defaultValue = "20") int limit) {
    return noticeService.recent(limit);
  }

  @GetMapping("/courses/{courseId}/notices")
  public List<NoticeView> forCourse(@PathVariable long courseId) {
    return noticeService.forCourse(courseId);
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

  /** Request to publish a notice; {@code courseId} null means a faculty-wide notice. */
  public record PublishNoticeRequest(Long courseId, String title, String body, boolean pinned) {}

  public record PublishedResponse(long id) {}
}
