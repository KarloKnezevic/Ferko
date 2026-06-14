package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;

class SmtpMailSenderTest {

  /** Captures the last SimpleMailMessage; the Spring MailSender interface has only two methods. */
  private static final class CapturingMailSender implements org.springframework.mail.MailSender {
    private final List<SimpleMailMessage> sent = new ArrayList<>();
    private boolean fail;

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
      if (fail) {
        throw new MailSendException("boom");
      }
      sent.add(simpleMessage);
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
      for (SimpleMailMessage m : simpleMessages) {
        send(m);
      }
    }
  }

  @Test
  void buildsAndSendsMessageWithFromRecipientsSubjectBody() {
    CapturingMailSender delegate = new CapturingMailSender();
    SmtpMailSender sender = new SmtpMailSender(delegate, "ferko@fer.hr");

    sender.send(List.of("a@fer.hr", "b@fer.hr"), "Naslov", "Tijelo");

    assertEquals(1, delegate.sent.size());
    SimpleMailMessage message = delegate.sent.get(0);
    assertEquals("ferko@fer.hr", message.getFrom());
    assertArrayEquals(new String[] {"a@fer.hr", "b@fer.hr"}, message.getTo());
    assertEquals("Naslov", message.getSubject());
    assertEquals("Tijelo", message.getText());
  }

  @Test
  void emptyRecipientsIsNoOp() {
    CapturingMailSender delegate = new CapturingMailSender();
    new SmtpMailSender(delegate, "ferko@fer.hr").send(List.of(), "x", "y");
    assertEquals(0, delegate.sent.size());
  }

  @Test
  void transportFailureIsSwallowed() {
    CapturingMailSender delegate = new CapturingMailSender();
    delegate.fail = true;
    SmtpMailSender sender = new SmtpMailSender(delegate, "ferko@fer.hr");
    // Best-effort: a MailException from the transport must not propagate.
    assertDoesNotThrow(() -> sender.send(List.of("a@fer.hr"), "Naslov", "Tijelo"));
    assertNotNull(sender);
  }
}
