package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.survey.SurveyService;
import hr.fer.zemris.ferko.application.usecase.survey.SurveyViews;
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
 * Course evaluation surveys ("ankete"): staff create surveys and read aggregated results; any
 * authenticated user (a student) may submit anonymous 1..5 ratings.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class SurveyController {

  private static final String CAN_MANAGE =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR')";

  private final SurveyService surveyService;

  public SurveyController(SurveyService surveyService) {
    this.surveyService = surveyService;
  }

  @GetMapping("/courses/{courseId}/surveys")
  public List<SurveyViews.SurveyView> list(@PathVariable long courseId) {
    return surveyService.listForCourse(courseId);
  }

  @PostMapping("/courses/{courseId}/surveys")
  @PreAuthorize(CAN_MANAGE)
  @ResponseStatus(HttpStatus.CREATED)
  public CreatedResponse create(
      @PathVariable long courseId, @RequestBody CreateSurveyRequest request) {
    try {
      return new CreatedResponse(
          surveyService.createSurvey(courseId, request.title(), request.questions()));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  @PostMapping("/surveys/{surveyId}/responses")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void submit(@PathVariable long surveyId, @RequestBody SubmitRequest request) {
    try {
      surveyService.submit(surveyId, request.answers());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  @GetMapping("/surveys/{surveyId}/results")
  @PreAuthorize(CAN_MANAGE)
  public List<SurveyViews.SurveyResultView> results(@PathVariable long surveyId) {
    return surveyService.results(surveyId);
  }

  public record CreateSurveyRequest(String title, List<String> questions) {}

  public record CreatedResponse(long id) {}

  public record SubmitRequest(List<SurveyViews.AnswerInput> answers) {}
}
