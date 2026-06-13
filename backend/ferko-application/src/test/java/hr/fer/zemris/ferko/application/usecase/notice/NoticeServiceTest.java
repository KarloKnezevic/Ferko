package hr.fer.zemris.ferko.application.usecase.notice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.NoticeRepository;
import hr.fer.zemris.ferko.domain.model.Notice;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class NoticeServiceTest {

  /** Minimal in-memory repository for the service test. */
  private static final class FakeNoticeRepository implements NoticeRepository {
    private final List<Notice> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public Notice save(Notice notice) {
      Notice saved =
          new Notice(
              ++seq,
              notice.courseId(),
              notice.title(),
              notice.body(),
              notice.authorName(),
              notice.createdAt(),
              notice.pinned());
      store.add(saved);
      return saved;
    }

    @Override
    public List<Notice> findRecent(int limit) {
      return store.stream()
          .sorted(
              Comparator.comparing(Notice::pinned)
                  .reversed()
                  .thenComparing(Comparator.comparing(Notice::createdAt).reversed()))
          .limit(limit)
          .toList();
    }

    @Override
    public List<Notice> findByCourse(long courseId) {
      return store.stream().filter(n -> Long.valueOf(courseId).equals(n.courseId())).toList();
    }
  }

  @Test
  void publishesAndListsRecentRespectingLimit() {
    NoticeService service = new NoticeService(new FakeNoticeRepository());

    long id = service.publish(null, "Naslov", "Tekst", true, "admin");
    assertTrue(id > 0);
    service.publish(7L, "Kolegij", "Tekst", false, "lecturer");

    List<NoticeView> recent = service.recent(10);
    assertEquals(2, recent.size());
    assertTrue(recent.get(0).pinned(), "pinned notice should be listed first");

    List<NoticeView> course = service.forCourse(7L);
    assertEquals(1, course.size());
    assertEquals(7L, course.get(0).courseId());
  }

  @Test
  void recentLimitIsClampedToAtLeastOne() {
    NoticeService service = new NoticeService(new FakeNoticeRepository());
    service.publish(null, "A", "x", false, "admin");
    service.publish(null, "B", "x", false, "admin");
    // A non-positive limit must not throw and must return at least one row.
    assertEquals(1, service.recent(0).size());
  }
}
