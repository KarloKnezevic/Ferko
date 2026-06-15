package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.academic.AcademicQueryService;
import hr.fer.zemris.ferko.application.usecase.academic.CourseDetailView;
import hr.fer.zemris.ferko.application.usecase.academic.CourseSummaryView;
import hr.fer.zemris.ferko.application.usecase.academic.EnrollmentView;
import hr.fer.zemris.ferko.application.usecase.academic.RoomView;
import hr.fer.zemris.ferko.application.usecase.academic.SemesterView;
import hr.fer.zemris.ferko.application.usecase.academic.StudentView;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Read-only REST API over the academic data model. */
@RestController
@RequestMapping("/api/v1/academic")
public class AcademicController {

  /** Roles that may browse the faculty-wide student roster. Students may not enumerate it. */
  private static final String STAFF =
      "hasAnyRole('ADMIN', 'STUSLU', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT')";

  private final AcademicQueryService query;
  private final CourseAccessGuard courseAccessGuard;

  public AcademicController(AcademicQueryService query, CourseAccessGuard courseAccessGuard) {
    this.query = query;
    this.courseAccessGuard = courseAccessGuard;
  }

  @GetMapping("/semesters")
  public List<SemesterView> semesters() {
    return query.listSemesters();
  }

  @GetMapping("/semesters/active")
  public SemesterView activeSemester() {
    return query
        .activeSemester()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nema aktivnog semestra."));
  }

  @GetMapping("/courses")
  public List<CourseSummaryView> courses(
      @RequestParam(required = false) String semester, Authentication authentication) {
    return query.listCoursesForPrincipal(
        authentication.getName(), rolesOf(authentication), semester);
  }

  private static Set<String> rolesOf(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> authority.startsWith("ROLE_"))
        .map(authority -> authority.substring("ROLE_".length()))
        .collect(Collectors.toSet());
  }

  @GetMapping("/courses/{id}")
  public CourseDetailView course(@PathVariable long id, Authentication authentication) {
    courseAccessGuard.requireCourseAccess(authentication, id);
    return query
        .courseDetail(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kolegij ne postoji."));
  }

  @GetMapping("/courses/{id}/enrollments")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'STUSLU', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT')")
  public List<EnrollmentView> enrollments(@PathVariable long id) {
    return query.listEnrollments(id);
  }

  @GetMapping("/students")
  @PreAuthorize(STAFF)
  public List<StudentView> students() {
    return query.listStudents();
  }

  @GetMapping("/students/{jmbag}")
  @PreAuthorize(STAFF)
  public StudentView student(@PathVariable String jmbag) {
    return query
        .getStudentByJmbag(jmbag)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student ne postoji."));
  }

  @GetMapping("/rooms")
  @PreAuthorize(STAFF)
  public List<RoomView> rooms() {
    return query.listRooms();
  }
}
