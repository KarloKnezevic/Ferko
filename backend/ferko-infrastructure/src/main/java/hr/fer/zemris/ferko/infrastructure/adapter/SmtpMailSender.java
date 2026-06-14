package hr.fer.zemris.ferko.infrastructure.adapter;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

/**
 * SMTP-backed {@link hr.fer.zemris.ferko.application.port.MailSender}. Delegates to Spring's {@link
 * org.springframework.mail.MailSender} (typically a {@code JavaMailSenderImpl} auto-configured from
 * {@code spring.mail.*}). Sending is best-effort: a transport failure is logged and swallowed so it
 * never breaks the business action that triggered the notification.
 */
public class SmtpMailSender implements hr.fer.zemris.ferko.application.port.MailSender {

  private static final Logger LOG = System.getLogger(SmtpMailSender.class.getName());

  private final org.springframework.mail.MailSender delegate;
  private final String from;

  public SmtpMailSender(org.springframework.mail.MailSender delegate, String from) {
    this.delegate = delegate;
    this.from = from;
  }

  @Override
  public void send(List<String> recipients, String subject, String body) {
    if (recipients == null || recipients.isEmpty()) {
      return;
    }
    SimpleMailMessage message = new SimpleMailMessage();
    if (from != null && !from.isBlank()) {
      message.setFrom(from);
    }
    message.setTo(recipients.toArray(new String[0]));
    message.setSubject(subject);
    message.setText(body);
    try {
      delegate.send(message);
    } catch (MailException ex) {
      LOG.log(
          Level.WARNING, () -> "Failed to send e-mail to " + recipients.size() + " recipient(s)");
    }
  }
}
