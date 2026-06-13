package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.grading.AutoGrader;
import hr.fer.zemris.ferko.application.grading.GradedSubmission;
import hr.fer.zemris.ferko.application.grading.ScoringPolicy;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auto-grades scanned multiple-choice answer sheets for an assessment against a correct-answer key
 * and scoring policy (the FERKO "ocjenjivanje provjere" computation), returning per-student scores.
 */
@RestController
@RequestMapping("/api/v1/academic/exams/{examId}")
public class ExamGradingController {

  @PostMapping("/auto-grade")
  @PreAuthorize("hasAnyRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT')")
  public List<GradedResult> autoGrade(
      @PathVariable long examId, @RequestBody AutoGradeRequest request) {
    ScoringPolicy policy =
        request.policy() == null ? ScoringPolicy.standard() : request.policy().toPolicy();
    return request.submissions().stream()
        .map(
            submission -> {
              GradedSubmission graded =
                  AutoGrader.grade(request.correctAnswers(), submission.answers(), policy);
              return new GradedResult(
                  submission.jmbag(), graded.total(), graded.correct(), graded.outcomes().size());
            })
        .toList();
  }

  /** Correct-answer key, scoring policy and the submissions to grade. */
  public record AutoGradeRequest(
      List<String> correctAnswers, PolicyDto policy, List<Submission> submissions) {}

  /** A single student's answer sheet. */
  public record Submission(String jmbag, List<String> answers) {}

  /** Scoring policy DTO. */
  public record PolicyDto(double correctPoints, double incorrectPoints, double blankPoints) {
    ScoringPolicy toPolicy() {
      return new ScoringPolicy(correctPoints, incorrectPoints, blankPoints);
    }
  }

  /** Per-student grading result. */
  public record GradedResult(String jmbag, double total, long correct, int questions) {}
}
