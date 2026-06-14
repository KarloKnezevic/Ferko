package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.audit.AuditEventView;
import hr.fer.zemris.ferko.application.usecase.audit.AuditService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read access to the academic audit trail (ADMIN only). */
@RestController
@RequestMapping("/api/v1/academic")
public class AuditController {

  private final AuditService auditService;

  public AuditController(AuditService auditService) {
    this.auditService = auditService;
  }

  @GetMapping("/audit")
  @PreAuthorize("hasRole('ADMIN')")
  public List<AuditEventView> recent(@RequestParam(defaultValue = "100") int limit) {
    return auditService.recent(limit);
  }
}
