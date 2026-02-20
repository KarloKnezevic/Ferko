package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.PingUseCase;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PingController {

  private final PingUseCase pingUseCase;

  public PingController(PingUseCase pingUseCase) {
    this.pingUseCase = pingUseCase;
  }

  @GetMapping("/ping")
  public Map<String, String> ping() {
    return Map.of("status", "ok", "message", pingUseCase.execute());
  }
}
