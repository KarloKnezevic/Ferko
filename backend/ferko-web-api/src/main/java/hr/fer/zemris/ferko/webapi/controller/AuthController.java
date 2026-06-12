package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.webapi.auth.FerkoPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Session-based authentication endpoints for the FERKO web client. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository securityContextRepository;

  public AuthController(
      AuthenticationManager authenticationManager,
      SecurityContextRepository securityContextRepository) {
    this.authenticationManager = authenticationManager;
    this.securityContextRepository = securityContextRepository;
  }

  @PostMapping("/login")
  public CurrentUserResponse login(
      @RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    Authentication authentication;
    try {
      authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.username(), request.password()));
    } catch (BadCredentialsException ex) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Neispravno korisničko ime ili lozinka.");
    }

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    httpRequest.getSession(true);
    securityContextRepository.saveContext(context, httpRequest, httpResponse);
    return toResponse(authentication);
  }

  @GetMapping("/me")
  public CurrentUserResponse me(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof FerkoPrincipal)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Prijava je obavezna.");
    }
    return toResponse(authentication);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(HttpServletRequest httpRequest) {
    HttpSession session = httpRequest.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    SecurityContextHolder.clearContext();
  }

  private CurrentUserResponse toResponse(Authentication authentication) {
    FerkoPrincipal principal = (FerkoPrincipal) authentication.getPrincipal();
    return new CurrentUserResponse(
        principal.id(), principal.getUsername(), principal.fullName(), principal.roleNames());
  }

  /** Login payload. */
  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

  /** Authenticated user projection returned to the client. */
  public record CurrentUserResponse(
      long id, String username, String fullName, List<String> roles) {}
}
