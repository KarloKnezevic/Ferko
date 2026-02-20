package hr.fer.zemris.ferko.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PingUseCaseTest {

  @Test
  void returnsExpectedPingMessage() {
    assertEquals("ferko-modernization-phase1", new PingUseCase().execute());
  }
}
