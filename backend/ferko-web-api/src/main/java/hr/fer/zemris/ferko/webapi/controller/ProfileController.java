package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.profile.AcademicSummaryService;
import hr.fer.zemris.ferko.application.usecase.profile.MyProfileView;
import hr.fer.zemris.ferko.application.usecase.profile.ProfileService;
import hr.fer.zemris.ferko.application.usecase.profile.StudentStudySummaryView;
import hr.fer.zemris.ferko.application.usecase.profile.TeachingLoadView;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The signed-in user's personal data ("Osobni podaci") and academic summaries. */
@RestController
@RequestMapping("/api/v1/academic")
public class ProfileController {

  private final ProfileService profileService;
  private final AcademicSummaryService summaryService;

  public ProfileController(ProfileService profileService, AcademicSummaryService summaryService) {
    this.profileService = profileService;
    this.summaryService = summaryService;
  }

  @GetMapping("/my/profile")
  public MyProfileView myProfile(Authentication authentication) {
    return profileService
        .forUser(authentication.getName())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji."));
  }

  @GetMapping("/my/study-summary")
  public StudentStudySummaryView studySummary(Authentication authentication) {
    return summaryService.studySummary(authentication.getName());
  }

  @GetMapping("/my/teaching-load")
  public TeachingLoadView teachingLoad(Authentication authentication) {
    return summaryService.teachingLoad(authentication.getName());
  }
}
