package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.profile.MyProfileView;
import hr.fer.zemris.ferko.application.usecase.profile.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The signed-in user's personal data ("Osobni podaci"). */
@RestController
@RequestMapping("/api/v1/academic")
public class ProfileController {

  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping("/my/profile")
  public MyProfileView myProfile(Authentication authentication) {
    return profileService
        .forUser(authentication.getName())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji."));
  }
}
