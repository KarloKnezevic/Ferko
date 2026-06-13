package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.ForumPost;
import java.util.List;

/** Persistence port for per-course discussions ("Pitanja i problemi"). */
public interface ForumRepository {

  ForumPost save(ForumPost post);

  /** Posts for a course, oldest first (questions and their answers interleaved by time). */
  List<ForumPost> findByCourse(long courseId);
}
