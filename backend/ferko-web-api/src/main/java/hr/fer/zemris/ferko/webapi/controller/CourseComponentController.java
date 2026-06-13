package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.component.CourseComponentService;
import hr.fer.zemris.ferko.application.usecase.component.CourseComponentView;
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
import org.springframework.web.server.ResponseStatusException;

/**
 * Course content components ("KOMPONENTE"): titled blocks on the course page. Reading returns
 * visible blocks to anyone authenticated; teaching staff add blocks.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class CourseComponentController {

  private static final String CAN_MANAGE =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR')";

  private final CourseComponentService componentService;

  public CourseComponentController(CourseComponentService componentService) {
    this.componentService = componentService;
  }

  @GetMapping("/courses/{courseId}/components")
  public List<CourseComponentView> list(@PathVariable long courseId) {
    return componentService.forCourse(courseId);
  }

  @PostMapping("/courses/{courseId}/components")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.CREATED)
  public CreatedResponse add(
      @PathVariable long courseId, @RequestBody CreateComponentRequest request) {
    try {
      return new CreatedResponse(
          componentService.add(
              courseId, request.title(), request.content(), request.ordinal(), request.visible()));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  public record CreateComponentRequest(
      String title, String content, int ordinal, boolean visible) {}

  public record CreatedResponse(long id) {}
}
