package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.student.MyExamView;
import hr.fer.zemris.ferko.application.usecase.student.StudentExamService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The signed-in student's own assessments ("Moje provjere"): every exam of their enrolled courses,
 * including the personal room and seat once the schedule is published.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class StudentExamController {

  private final StudentExamService studentExamService;

  public StudentExamController(StudentExamService studentExamService) {
    this.studentExamService = studentExamService;
  }

  @GetMapping("/my/exams")
  public List<MyExamView> myExams(Authentication authentication) {
    return studentExamService.forStudent(authentication.getName());
  }
}
