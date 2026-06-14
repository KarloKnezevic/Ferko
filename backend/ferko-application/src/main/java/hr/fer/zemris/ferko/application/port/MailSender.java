package hr.fer.zemris.ferko.application.port;

import java.util.List;

/**
 * Outbound e-mail port. Adapters decide the transport: a dev/logging sender by default, an SMTP
 * sender in production.
 */
public interface MailSender {

  /** Sends a plain-text message to the given recipients (no-op when the list is empty). */
  void send(List<String> recipients, String subject, String body);
}
