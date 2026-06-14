package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.MailSender;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

/**
 * Default {@link MailSender} for dev/test: records the intent to send (recipient count and subject)
 * to the application log instead of contacting an SMTP server. An SMTP-backed adapter is wired in
 * production via configuration.
 */
public class LoggingMailSender implements MailSender {

  private static final Logger LOG = System.getLogger(LoggingMailSender.class.getName());

  @Override
  public void send(List<String> recipients, String subject, String body) {
    if (recipients == null || recipients.isEmpty()) {
      return;
    }
    LOG.log(
        Level.INFO,
        () ->
            "[mail:dev] Would send to " + recipients.size() + " recipient(s); subject: " + subject);
  }
}
