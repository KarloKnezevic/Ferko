package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.access.AccessControlService;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Web-layer helper that enforces row-level course access by delegating to {@link
 * AccessControlService}. Role-based coarse checks stay in {@code @PreAuthorize}; this guard adds
 * the per-course "is this user actually related to the course" check that annotations cannot
 * express.
 */
@Component
public class CourseAccessGuard {

  private final AccessControlService accessControl;

  public CourseAccessGuard(AccessControlService accessControl) {
    this.accessControl = accessControl;
  }

  /** Throws {@code 403} unless the authenticated user may view the given course's content. */
  public void requireCourseAccess(Authentication authentication, long courseId) {
    if (authentication == null
        || !accessControl.canAccessCourse(
            authentication.getName(), rolesOf(authentication), courseId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pristup ovom kolegiju.");
    }
  }

  private static Set<String> rolesOf(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> authority.startsWith("ROLE_"))
        .map(authority -> authority.substring("ROLE_".length()))
        .collect(Collectors.toSet());
  }
}
