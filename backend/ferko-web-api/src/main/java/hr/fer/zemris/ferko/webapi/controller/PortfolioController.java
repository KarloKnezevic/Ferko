package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.portfolio.PortfolioEntryView;
import hr.fer.zemris.ferko.application.usecase.portfolio.PortfolioService;
import java.util.List;
import org.springframework.http.HttpStatus;
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

/** The signed-in user's e-portfolio: each user manages only their own entries. */
@RestController
@RequestMapping("/api/v1/academic")
public class PortfolioController {

  private final PortfolioService portfolioService;

  public PortfolioController(PortfolioService portfolioService) {
    this.portfolioService = portfolioService;
  }

  @GetMapping("/my/portfolio")
  public List<PortfolioEntryView> list(Authentication authentication) {
    return portfolioService.forUser(authentication.getName());
  }

  @PostMapping("/my/portfolio")
  @ResponseStatus(HttpStatus.CREATED)
  public CreatedResponse add(
      @RequestBody CreateEntryRequest request, Authentication authentication) {
    try {
      return new CreatedResponse(
          portfolioService.add(
              authentication.getName(),
              request.title(),
              request.description(),
              request.category(),
              request.link()));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  @DeleteMapping("/my/portfolio/{entryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(@PathVariable long entryId, Authentication authentication) {
    portfolioService.remove(authentication.getName(), entryId);
  }

  public record CreateEntryRequest(
      String title, String description, String category, String link) {}

  public record CreatedResponse(long id) {}
}
