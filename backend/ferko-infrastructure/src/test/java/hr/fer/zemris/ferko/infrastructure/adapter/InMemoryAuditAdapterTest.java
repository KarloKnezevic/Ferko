package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hr.fer.zemris.ferko.application.port.ToDoAuditAction;
import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditOutcome;
import org.junit.jupiter.api.Test;

class InMemoryAuditAdapterTest {

  @Test
  void returnsInMemorySource() {
    assertEquals("in-memory", new InMemoryAuditAdapter().source());
  }

  @Test
  void storesAuditEventsInMemory() {
    InMemoryAuditAdapter adapter = new InMemoryAuditAdapter();

    adapter.log(
        new ToDoAuditEvent(
            ToDoAuditAction.CREATE, ToDoAuditOutcome.SUCCESS, 11L, 44L, "task created"));

    assertEquals(1, adapter.events().size());
    assertEquals(ToDoAuditAction.CREATE, adapter.events().getFirst().action());
  }
}
