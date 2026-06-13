package hr.fer.zemris.ferko.application.usecase.forum;

import hr.fer.zemris.ferko.application.port.ForumRepository;
import hr.fer.zemris.ferko.domain.model.ForumPost;
import java.time.LocalDateTime;
import java.util.List;

/** Posts and lists messages in per-course discussions ("Pitanja i problemi"). */
public class ForumService {

  private final ForumRepository forumRepository;

  public ForumService(ForumRepository forumRepository) {
    this.forumRepository = forumRepository;
  }

  public List<ForumPostView> forCourse(long courseId) {
    return forumRepository.findByCourse(courseId).stream().map(ForumService::toView).toList();
  }

  public long post(long courseId, Long parentId, String body, String authorName) {
    if (body == null || body.isBlank()) {
      throw new IllegalArgumentException("Poruka ne smije biti prazna.");
    }
    ForumPost saved =
        forumRepository.save(
            new ForumPost(0L, courseId, parentId, authorName, body, LocalDateTime.now()));
    return saved.id();
  }

  private static ForumPostView toView(ForumPost post) {
    return new ForumPostView(
        post.id(),
        post.courseId(),
        post.parentId(),
        post.authorName(),
        post.body(),
        post.createdAt());
  }
}
