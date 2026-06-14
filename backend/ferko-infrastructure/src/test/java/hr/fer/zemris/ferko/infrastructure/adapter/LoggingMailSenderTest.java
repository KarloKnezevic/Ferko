package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import org.junit.jupiter.api.Test;

class LoggingMailSenderTest {

  private final LoggingMailSender sender = new LoggingMailSender();

  @Test
  void sendsToRecipientsWithoutError() {
    assertDoesNotThrow(
        () -> sender.send(List.of("a@fer.hr", "b@fer.hr"), "Naslov", "Tijelo poruke"));
  }

  @Test
  void emptyOrNullRecipientsAreNoOp() {
    assertDoesNotThrow(() -> sender.send(List.of(), "Naslov", "Tijelo"));
    assertDoesNotThrow(() -> sender.send(null, "Naslov", "Tijelo"));
  }
}
