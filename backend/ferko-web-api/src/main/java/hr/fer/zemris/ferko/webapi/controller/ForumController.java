package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.forum.ForumPostView;
import hr.fer.zemris.ferko.application.usecase.forum.ForumService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Per-course discussion ("Pitanja i problemi"): any authenticated user may read and post; a post
 * may answer another via {@code parentId}.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class ForumController {

  private final ForumService forumService;

  public ForumController(ForumService forumService) {
    this.forumService = forumService;
  }

  @GetMapping("/courses/{courseId}/forum")
  public List<ForumPostView> list(@PathVariable long courseId) {
    return forumService.forCourse(courseId);
  }

  @PostMapping("/courses/{courseId}/forum")
  @ResponseStatus(HttpStatus.CREATED)
  public CreatedResponse post(
      @PathVariable long courseId,
      @RequestBody CreatePostRequest request,
      Authentication authentication) {
    try {
      return new CreatedResponse(
          forumService.post(
              courseId, request.parentId(), request.body(), authentication.getName()));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  public record CreatePostRequest(Long parentId, String body) {}

  public record CreatedResponse(long id) {}
}
