package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.exam.ExamSchedulingService;
import hr.fer.zemris.ferko.application.usecase.student.MyExamView;
import hr.fer.zemris.ferko.application.usecase.student.StudentExamService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The signed-in student's own assessments ("Moje provjere"): every exam of their enrolled courses,
 * including the personal room and seat once the schedule is published.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class StudentExamController {

  private final StudentExamService studentExamService;
  private final ExamSchedulingService examSchedulingService;

  public StudentExamController(
      StudentExamService studentExamService, ExamSchedulingService examSchedulingService) {
    this.studentExamService = studentExamService;
    this.examSchedulingService = examSchedulingService;
  }

  @GetMapping("/my/exams")
  public List<MyExamView> myExams(Authentication authentication) {
    return studentExamService.forStudent(authentication.getName());
  }

  @PostMapping("/my/exams/{examId}/registration")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void register(@PathVariable long examId, Authentication authentication) {
    boolean ok = invoke(() -> examSchedulingService.registerSelf(examId, authentication.getName()));
    if (!ok) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Provjera ne postoji ili niste upisani na kolegij.");
    }
  }

  @DeleteMapping("/my/exams/{examId}/registration")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unregister(@PathVariable long examId, Authentication authentication) {
    boolean ok =
        invoke(() -> examSchedulingService.unregisterSelf(examId, authentication.getName()));
    if (!ok) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provjera ne postoji.");
    }
  }

  private static boolean invoke(java.util.function.BooleanSupplier action) {
    try {
      return action.getAsBoolean();
    } catch (IllegalStateException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
    }
  }
}
