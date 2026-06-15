package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.academic.AcademicProvisioningService;
import hr.fer.zemris.ferko.application.usecase.academic.AcademicQueryService;
import hr.fer.zemris.ferko.application.usecase.academic.AppUserView;
import hr.fer.zemris.ferko.application.usecase.academic.SemesterView;
import hr.fer.zemris.ferko.application.usecase.academic.StudentView;
import hr.fer.zemris.ferko.application.usecase.academic.SyncStatusView;
import hr.fer.zemris.ferko.application.usecase.admin.AdminStudentService;
import hr.fer.zemris.ferko.application.usecase.admin.AdminStudentViews.AdminStudentProfileView;
import hr.fer.zemris.ferko.application.usecase.admin.AdminStudentViews.PasswordResetView;
import hr.fer.zemris.ferko.application.usecase.audit.AuditService;
import hr.fer.zemris.ferko.webapi.support.TemporaryPasswords;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

/** Administrative write operations over the academic data model. */
@RestController
@RequestMapping("/api/v1/academic")
public class AcademicAdminController {

  private final AcademicProvisioningService provisioning;
  private final AcademicQueryService query;
  private final AdminStudentService adminStudent;
  private final AuditService audit;

  public AcademicAdminController(
      AcademicProvisioningService provisioning,
      AcademicQueryService query,
      AdminStudentService adminStudent,
      AuditService audit) {
    this.provisioning = provisioning;
    this.query = query;
    this.adminStudent = adminStudent;
    this.audit = audit;
  }

  @PostMapping("/courses")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public CourseCreatedResponse createCourse(
      @RequestBody CreateCourseRequest request, Authentication authentication) {
    String semester =
        query
            .activeSemester()
            .map(SemesterView::code)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.CONFLICT, "Nema aktivnog semestra."));
    long id =
        provisioning.provisionCourse(
            request.code(),
            request.name(),
            semester,
            request.ects(),
            request.description(),
            request.literature());
    audit.record(
        authentication.getName(), "COURSE_CREATED", "course", String.valueOf(id), request.code());
    return new CourseCreatedResponse(id, request.code(), semester);
  }

  @PostMapping("/courses/{courseId}/enrollments")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUSLU')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void enroll(
      @PathVariable long courseId,
      @RequestBody EnrollRequest request,
      Authentication authentication) {
    long studentId =
        query
            .getStudentByJmbag(request.jmbag())
            .map(StudentView::id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student ne postoji."));
    provisioning.enroll(studentId, courseId, LocalDateTime.now());
    audit.record(
        authentication.getName(),
        "STUDENT_ENROLLED",
        "course",
        String.valueOf(courseId),
        "jmbag=" + request.jmbag());
  }

  @PostMapping("/courses/{courseId}/group-assignments")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUSLU', 'NOSITELJ')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assignGroup(
      @PathVariable long courseId,
      @RequestBody AssignGroupRequest request,
      Authentication authentication) {
    boolean assigned =
        provisioning.assignStudentToGroup(courseId, request.jmbag(), request.groupId());
    if (!assigned) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Student, upis ili grupa ne postoje.");
    }
    audit.record(
        authentication.getName(),
        "GROUP_ASSIGNED",
        "course",
        String.valueOf(courseId),
        "jmbag=" + request.jmbag() + ", group=" + request.groupId());
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  public List<AppUserView> listUsers() {
    return query.listUsers();
  }

  @GetMapping("/users/{userId}/profile")
  @PreAuthorize("hasRole('ADMIN')")
  public AdminStudentProfileView userProfile(@PathVariable long userId) {
    return adminStudent
        .profile(userId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji."));
  }

  @PostMapping("/users/{userId}/reset-password")
  @PreAuthorize("hasRole('ADMIN')")
  public PasswordResetView resetUserPassword(
      @PathVariable long userId, Authentication authentication) {
    PasswordResetView result =
        adminStudent
            .resetPassword(userId, TemporaryPasswords.generate())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji."));
    audit.record(
        authentication.getName(),
        "USER_PASSWORD_RESET",
        "app_user",
        String.valueOf(userId),
        "reset by admin");
    return result;
  }

  @GetMapping("/sync/status")
  @PreAuthorize("hasRole('ADMIN')")
  public SyncStatusView syncStatus() {
    return query.syncStatus();
  }

  @PostMapping("/courses/{courseId}/staff")
  @PreAuthorize("hasAnyRole('ADMIN', 'NOSITELJ')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assignStaff(
      @PathVariable long courseId,
      @RequestBody AssignStaffRequest request,
      Authentication authentication) {
    boolean assigned =
        provisioning.assignStaffByUsername(courseId, request.username(), request.role());
    if (!assigned) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji.");
    }
    audit.record(
        authentication.getName(),
        "STAFF_ASSIGNED",
        "course",
        String.valueOf(courseId),
        request.username() + " as " + request.role());
  }

  @PostMapping("/semesters")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public void createSemester(
      @RequestBody CreateSemesterRequest request, Authentication authentication) {
    provisioning.provisionSemester(
        request.code(),
        request.academicYear(),
        request.term(),
        LocalDate.parse(request.startsOn()),
        LocalDate.parse(request.endsOn()),
        request.active());
    audit.record(authentication.getName(), "SEMESTER_CREATED", "semester", request.code(), null);
  }

  /** Request to assign a teaching role on a course to a user (by username). */
  public record AssignStaffRequest(@NotBlank String username, @NotBlank String role) {}

  /** Request to create/update a semester. Dates are ISO {@code yyyy-MM-dd}. */
  public record CreateSemesterRequest(
      @NotBlank String code,
      @NotBlank String academicYear,
      @NotBlank String term,
      @NotBlank String startsOn,
      @NotBlank String endsOn,
      boolean active) {}

  /** Request to create a course in the active semester. */
  public record CreateCourseRequest(
      @NotBlank String code,
      @NotBlank String name,
      int ects,
      String description,
      String literature) {}

  /** Result of creating a course. */
  public record CourseCreatedResponse(long id, String code, String semesterCode) {}

  /** Request to enroll a student (by JMBAG) into a course. */
  public record EnrollRequest(@NotBlank String jmbag) {}

  /** Request to assign a student (by JMBAG) to a group of the course. */
  public record AssignGroupRequest(@NotBlank String jmbag, long groupId) {}
}
