package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryAuditAdapter implements ToDoAuditLogPort {

  private final List<ToDoAuditEvent> events = new CopyOnWriteArrayList<>();

  @Override
  public void log(ToDoAuditEvent event) {
    events.add(event);
  }

  public List<ToDoAuditEvent> events() {
    return List.copyOf(events);
  }

  public String source() {
    return "in-memory";
  }
}
