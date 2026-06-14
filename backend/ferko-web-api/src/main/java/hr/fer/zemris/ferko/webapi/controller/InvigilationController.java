package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.exam.InvigilationService;
import hr.fer.zemris.ferko.application.usecase.exam.MyDutyView;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The signed-in staff member's invigilation duties ("Moja dežurstva"). */
@RestController
@RequestMapping("/api/v1/academic")
public class InvigilationController {

  private final InvigilationService invigilationService;

  public InvigilationController(InvigilationService invigilationService) {
    this.invigilationService = invigilationService;
  }

  @GetMapping("/my/duties")
  public List<MyDutyView> myDuties(Authentication authentication) {
    return invigilationService.myDuties(authentication.getName());
  }
}
