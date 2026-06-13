package hr.fer.zemris.ferko.webapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards client-side SPA routes to {@code index.html} so deep links and browser refreshes resolve
 * to the single-page application. The root path and static assets are served by Spring Boot's
 * default static resource handling; REST, actuator and API-docs paths keep their own controllers.
 */
@Controller
public class SpaForwardingController {

  @GetMapping({"/login", "/kolegiji/**", "/prostorije", "/studenti"})
  public String forwardToIndex() {
    return "forward:/index.html";
  }
}
