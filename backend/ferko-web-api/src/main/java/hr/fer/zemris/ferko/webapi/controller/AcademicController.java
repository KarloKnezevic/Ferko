package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.academic.AcademicQueryService;
import hr.fer.zemris.ferko.application.usecase.academic.CourseDetailView;
import hr.fer.zemris.ferko.application.usecase.academic.CourseSummaryView;
import hr.fer.zemris.ferko.application.usecase.academic.EnrollmentView;
import hr.fer.zemris.ferko.application.usecase.academic.RoomView;
import hr.fer.zemris.ferko.application.usecase.academic.SemesterView;
import hr.fer.zemris.ferko.application.usecase.academic.StudentView;
import java.util.List;
import org.springframework.http.HttpStatus;
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

  private final AcademicQueryService query;

  public AcademicController(AcademicQueryService query) {
    this.query = query;
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
  public List<CourseSummaryView> courses(@RequestParam(required = false) String semester) {
    return query.listCourses(semester);
  }

  @GetMapping("/courses/{id}")
  public CourseDetailView course(@PathVariable long id) {
    return query
        .courseDetail(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kolegij ne postoji."));
  }

  @GetMapping("/courses/{id}/enrollments")
  public List<EnrollmentView> enrollments(@PathVariable long id) {
    return query.listEnrollments(id);
  }

  @GetMapping("/students")
  public List<StudentView> students() {
    return query.listStudents();
  }

  @GetMapping("/students/{jmbag}")
  public StudentView student(@PathVariable String jmbag) {
    return query
        .getStudentByJmbag(jmbag)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student ne postoji."));
  }

  @GetMapping("/rooms")
  public List<RoomView> rooms() {
    return query.listRooms();
  }
}
