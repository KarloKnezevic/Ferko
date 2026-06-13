package hr.fer.zemris.ferko.application.usecase.forum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ForumRepository;
import hr.fer.zemris.ferko.domain.model.ForumPost;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ForumServiceTest {

  private static final class FakeForumRepository implements ForumRepository {
    private final List<ForumPost> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public ForumPost save(ForumPost post) {
      ForumPost saved =
          new ForumPost(
              ++seq,
              post.courseId(),
              post.parentId(),
              post.authorName(),
              post.body(),
              post.createdAt());
      store.add(saved);
      return saved;
    }

    @Override
    public List<ForumPost> findByCourse(long courseId) {
      return store.stream().filter(p -> p.courseId() == courseId).toList();
    }
  }

  @Test
  void postsAndListsDiscussion() {
    ForumService service = new ForumService(new FakeForumRepository());
    long questionId = service.post(5L, null, "Pitanje?", "student.ana");
    assertTrue(questionId > 0);
    service.post(5L, questionId, "Odgovor.", "lecturer.marko");

    List<ForumPostView> posts = service.forCourse(5L);
    assertEquals(2, posts.size());
    assertEquals(questionId, posts.get(1).parentId());
  }

  @Test
  void rejectsEmptyBody() {
    ForumService service = new ForumService(new FakeForumRepository());
    assertThrows(IllegalArgumentException.class, () -> service.post(5L, null, "  ", "ana"));
  }
}
