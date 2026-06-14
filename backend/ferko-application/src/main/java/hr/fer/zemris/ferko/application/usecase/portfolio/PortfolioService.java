package hr.fer.zemris.ferko.application.usecase.portfolio;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.PortfolioRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.PortfolioEntry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Manages the signed-in user's e-portfolio (projects, achievements, skills). */
public class PortfolioService {

  private final PortfolioRepository portfolioRepository;
  private final AppUserRepository userRepository;

  public PortfolioService(
      PortfolioRepository portfolioRepository, AppUserRepository userRepository) {
    this.portfolioRepository = portfolioRepository;
    this.userRepository = userRepository;
  }

  public List<PortfolioEntryView> forUser(String username) {
    return userRepository
        .findByUsername(username)
        .map(
            user ->
                portfolioRepository.findByUser(user.id()).stream()
                    .map(PortfolioService::toView)
                    .toList())
        .orElseGet(List::of);
  }

  /**
   * Adds an entry to the signed-in user's portfolio. Returns 0 when the user is unknown; throws
   * {@link IllegalArgumentException} on a blank title.
   */
  public long add(String username, String title, String description, String category, String link) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Naslov je obavezan.");
    }
    Optional<AppUser> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return 0L;
    }
    return portfolioRepository
        .save(
            new PortfolioEntry(
                0L,
                user.get().id(),
                title.trim(),
                description == null ? "" : description.trim(),
                category == null ? "" : category.trim(),
                link == null ? "" : link.trim(),
                LocalDateTime.now()))
        .id();
  }

  /** Removes one of the user's own entries (no-op if it is not theirs). */
  public void remove(String username, long entryId) {
    userRepository
        .findByUsername(username)
        .ifPresent(user -> portfolioRepository.remove(user.id(), entryId));
  }

  private static PortfolioEntryView toView(PortfolioEntry entry) {
    return new PortfolioEntryView(
        entry.id(),
        entry.title(),
        entry.description(),
        entry.category(),
        entry.link(),
        entry.createdAt());
  }
}
