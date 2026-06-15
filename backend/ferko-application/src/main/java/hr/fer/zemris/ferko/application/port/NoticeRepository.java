package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Notice;
import java.util.List;
import java.util.Optional;

/** Persistence port for announcements ("obavijesti"). */
public interface NoticeRepository {

  Notice save(Notice notice);

  /** Most recent notices (pinned first), capped at {@code limit}. */
  List<Notice> findRecent(int limit);

  /** Notices for a course (pinned first, newest first). */
  List<Notice> findByCourse(long courseId);

  /** Single notice by id, if it exists. */
  Optional<Notice> findById(long id);

  /** Deletes the notice; returns {@code true} when a row was removed. */
  boolean deleteById(long id);
}
