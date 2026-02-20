package hr.fer.zemris.ferko.webapi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import hr.fer.zemris.ferko.webapi.portal.FerkoPortalService;
import hr.fer.zemris.ferko.webapi.portal.FerkoPortalService.ActionResult;
import hr.fer.zemris.ferko.webapi.portal.FerkoPortalService.AuthView;
import hr.fer.zemris.ferko.webapi.portal.FerkoPortalService.DemoAccountView;
import hr.fer.zemris.ferko.webapi.portal.FerkoPortalService.PortalRole;
import hr.fer.zemris.ferko.webapi.portal.FerkoPortalService.PortalSessionUser;
import hr.fer.zemris.ferko.webapi.portal.FerkoPortalService.WorkspaceView;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/portal")
public class FerkoPortalController {

  private static final String SESSION_USER_KEY = "ferko.portal.user";

  private final FerkoPortalService portalService;

  public FerkoPortalController(FerkoPortalService portalService) {
    this.portalService = portalService;
  }

  @PostMapping("/auth/login")
  public AuthView login(@RequestBody PortalLoginRequest request, HttpSession session) {
    AuthView authView =
        portalService
            .authenticate(request.username(), request.password())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid username or password."));

    PortalSessionUser sessionUser =
        new PortalSessionUser(
            authView.userId(),
            authView.username(),
            authView.fullName(),
            PortalRole.valueOf(authView.role()));
    session.setAttribute(SESSION_USER_KEY, sessionUser);
    return authView;
  }

  @GetMapping("/auth/me")
  public AuthView currentUser(HttpSession session) {
    PortalSessionUser sessionUser = requireSessionUser(session);
    return portalService.findUserById(sessionUser.userId());
  }

  @PostMapping("/auth/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(HttpSession session) {
    session.invalidate();
  }

  @GetMapping("/public/demo-accounts")
  public List<DemoAccountView> demoAccounts() {
    return portalService.demoAccountsView();
  }

  @GetMapping("/workspace")
  public WorkspaceView workspace(HttpSession session) {
    PortalSessionUser sessionUser = requireSessionUser(session);
    return portalService.buildWorkspace(sessionUser);
  }

  @PostMapping("/actions")
  public ActionResult performAction(@RequestBody PortalActionRequest request, HttpSession session) {
    PortalSessionUser sessionUser = requireSessionUser(session);
    return portalService.executeAction(sessionUser, request.type(), request.payload());
  }

  private PortalSessionUser requireSessionUser(HttpSession session) {
    Object rawSessionUser = session.getAttribute(SESSION_USER_KEY);
    if (!(rawSessionUser instanceof PortalSessionUser sessionUser)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login is required.");
    }
    return sessionUser;
  }

  public record PortalLoginRequest(String username, String password) {}

  public record PortalActionRequest(String type, JsonNode payload) {}
}
