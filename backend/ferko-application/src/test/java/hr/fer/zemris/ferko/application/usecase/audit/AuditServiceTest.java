package hr.fer.zemris.ferko.application.usecase.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.AuditEventRepository;
import hr.fer.zemris.ferko.domain.model.AcademicAuditEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class AuditServiceTest {

  private static final class FakeRepository implements AuditEventRepository {
    private final List<AcademicAuditEvent> store = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public AcademicAuditEvent save(AcademicAuditEvent e) {
      AcademicAuditEvent saved =
          new AcademicAuditEvent(
              seq.incrementAndGet(),
              e.occurredAt(),
              e.actor(),
              e.action(),
              e.entityType(),
              e.entityId(),
              e.details());
      store.add(saved);
      return saved;
    }

    @Override
    public List<AcademicAuditEvent> recent(int limit) {
      return store.stream()
          .sorted(Comparator.comparingLong(AcademicAuditEvent::id).reversed())
          .limit(limit)
          .toList();
    }
  }

  @Test
  void recordsAndReadsRecentEntries() {
    AuditService service = new AuditService(new FakeRepository());
    service.record("admin.ferko", "SEMESTER_CREATED", "semester", "2026L", null);
    service.record("admin.ferko", "COURSE_CREATED", "course", "7", "PROG");

    List<AuditEventView> recent = service.recent(10);
    assertEquals(2, recent.size());
    assertEquals("COURSE_CREATED", recent.get(0).action(), "newest first");
    assertEquals("admin.ferko", recent.get(0).actor());
    assertEquals("", recent.get(1).details(), "null details normalised to empty");
  }

  @Test
  void blankActorBecomesUnknownAndLimitIsApplied() {
    AuditService service = new AuditService(new FakeRepository());
    for (int i = 0; i < 5; i++) {
      service.record("  ", "ACTION_" + i, "x", String.valueOf(i), "d");
    }
    List<AuditEventView> recent = service.recent(2);
    assertEquals(2, recent.size());
    assertTrue(recent.stream().allMatch(e -> e.actor().equals("unknown")));
  }
}
