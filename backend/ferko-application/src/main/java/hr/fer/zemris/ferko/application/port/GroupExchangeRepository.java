package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.ExchangeStatus;
import hr.fer.zemris.ferko.domain.model.GroupExchangeRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Persistence port for group-exchange requests ("burza grupa"). */
public interface GroupExchangeRepository {

  GroupExchangeRequest save(GroupExchangeRequest request);

  List<GroupExchangeRequest> findByCourse(long courseId);

  Optional<GroupExchangeRequest> findById(long id);

  void updateDecision(long id, ExchangeStatus status, String decidedBy, LocalDateTime decidedAt);
}
