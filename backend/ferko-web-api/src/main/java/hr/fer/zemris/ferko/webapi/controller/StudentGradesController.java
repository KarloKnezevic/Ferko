package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.student.MyCourseGradeView;
import hr.fer.zemris.ferko.application.usecase.student.StudentGradesService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The signed-in student's own points and grades ("Moji bodovi"), aggregated across the courses they
 * are enrolled in.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class StudentGradesController {

  private final StudentGradesService studentGradesService;

  public StudentGradesController(StudentGradesService studentGradesService) {
    this.studentGradesService = studentGradesService;
  }

  @GetMapping("/my/grades")
  public List<MyCourseGradeView> myGrades(Authentication authentication) {
    return studentGradesService.forStudent(authentication.getName());
  }
}
