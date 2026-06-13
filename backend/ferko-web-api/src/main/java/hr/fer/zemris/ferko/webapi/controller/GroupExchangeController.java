package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.exchange.GroupExchangeService;
import hr.fer.zemris.ferko.application.usecase.exchange.GroupExchangeView;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Group-exchange marketplace ("burza grupa"): students post switch requests; teaching staff or
 * STUSLU decide. Listing is open to any authenticated user.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class GroupExchangeController {

  private static final String CAN_DECIDE =
      "hasAnyRole('ADMIN', 'NOSITELJ', 'ASISTENT_ORGANIZATOR', 'STUSLU')";

  private final GroupExchangeService exchangeService;

  public GroupExchangeController(GroupExchangeService exchangeService) {
    this.exchangeService = exchangeService;
  }

  @GetMapping("/courses/{courseId}/group-exchange")
  public List<GroupExchangeView> list(@PathVariable long courseId) {
    return exchangeService.listForCourse(courseId);
  }

  @PostMapping("/courses/{courseId}/group-exchange")
  @ResponseStatus(HttpStatus.CREATED)
  public CreatedResponse request(
      @PathVariable long courseId,
      @RequestBody RequestExchange request,
      Authentication authentication) {
    try {
      return new CreatedResponse(
          exchangeService.request(
              courseId,
              authentication.getName(),
              request.fromGroupId(),
              request.toGroupId(),
              request.reason()));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  @PostMapping("/group-exchange/{requestId}/decision")
  @PreAuthorize(CAN_DECIDE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void decide(
      @PathVariable long requestId,
      @RequestBody DecisionRequest decision,
      Authentication authentication) {
    try {
      exchangeService.decide(requestId, decision.approve(), authentication.getName());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    }
  }

  public record RequestExchange(Long fromGroupId, Long toGroupId, String reason) {}

  public record DecisionRequest(boolean approve) {}

  public record CreatedResponse(long id) {}
}
