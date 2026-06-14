package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.literature.CourseLiteratureService;
import hr.fer.zemris.ferko.application.usecase.literature.CourseLiteratureView;
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
 * Course reading list ("Literatura"): required and recommended entries on the course page. Reading
 * is open to anyone authenticated; teaching staff add entries.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class CourseLiteratureController {

  private static final String CAN_MANAGE =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR')";

  private final CourseLiteratureService literatureService;

  public CourseLiteratureController(CourseLiteratureService literatureService) {
    this.literatureService = literatureService;
  }

  @GetMapping("/courses/{courseId}/literature")
  public List<CourseLiteratureView> list(@PathVariable long courseId) {
    return literatureService.forCourse(courseId);
  }

  @PostMapping("/courses/{courseId}/literature")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.CREATED)
  public CreatedResponse add(
      @PathVariable long courseId, @RequestBody CreateLiteratureRequest request) {
    try {
      return new CreatedResponse(
          literatureService.add(
              courseId, request.title(), request.author(), request.mandatory(), request.ordinal()));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  public record CreateLiteratureRequest(
      String title, String author, boolean mandatory, int ordinal) {}

  public record CreatedResponse(long id) {}
}
