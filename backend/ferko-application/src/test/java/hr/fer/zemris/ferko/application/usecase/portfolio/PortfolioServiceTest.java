package hr.fer.zemris.ferko.application.usecase.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.PortfolioRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.PortfolioEntry;
import hr.fer.zemris.ferko.domain.model.Role;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortfolioServiceTest {

  private static final class FakeRepository implements PortfolioRepository {
    private final List<PortfolioEntry> store = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public PortfolioEntry save(PortfolioEntry e) {
      PortfolioEntry saved =
          new PortfolioEntry(
              seq.incrementAndGet(),
              e.userId(),
              e.title(),
              e.description(),
              e.category(),
              e.link(),
              e.createdAt());
      store.add(saved);
      return saved;
    }

    @Override
    public List<PortfolioEntry> findByUser(long userId) {
      return store.stream().filter(e -> e.userId() == userId).toList();
    }

    @Override
    public void remove(long userId, long entryId) {
      store.removeIf(e -> e.userId() == userId && e.id() == entryId);
    }
  }

  private InMemoryAcademicRepositories.Users users;
  private PortfolioService service;

  @BeforeEach
  void setUp() {
    users = new InMemoryAcademicRepositories.Users();
    service = new PortfolioService(new FakeRepository(), users);
    users.save(
        new AppUser(
            0L,
            "student.ana",
            "x",
            "Ana",
            "ana@fer.hr",
            true,
            LocalDateTime.now(),
            Set.of(Role.STUDENT)));
  }

  @Test
  void addsListsAndRemovesOwnEntries() {
    long id = service.add("student.ana", "Projekt X", "Opis", "PROJEKT", "http://x");
    assertTrue(id > 0);

    List<PortfolioEntryView> list = service.forUser("student.ana");
    assertEquals(1, list.size());
    assertEquals("Projekt X", list.get(0).title());
    assertEquals("PROJEKT", list.get(0).category());

    service.remove("student.ana", id);
    assertTrue(service.forUser("student.ana").isEmpty());
  }

  @Test
  void rejectsBlankTitle() {
    assertThrows(
        IllegalArgumentException.class, () -> service.add("student.ana", "  ", "x", "y", "z"));
  }

  @Test
  void unknownUserAddReturnsZeroAndListEmpty() {
    assertEquals(0L, service.add("ne.postoji", "T", "", "", ""));
    assertTrue(service.forUser("ne.postoji").isEmpty());
  }
}
