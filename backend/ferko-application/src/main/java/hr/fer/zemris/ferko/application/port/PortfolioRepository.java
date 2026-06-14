package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.PortfolioEntry;
import java.util.List;

/** Persistence port for user e-portfolio entries. */
public interface PortfolioRepository {

  PortfolioEntry save(PortfolioEntry entry);

  /** Entries owned by a user, newest first. */
  List<PortfolioEntry> findByUser(long userId);

  /** Removes an entry owned by the user (no-op if it is not theirs). */
  void remove(long userId, long entryId);
}
