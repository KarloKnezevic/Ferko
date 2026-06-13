package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.ForumRepository;
import hr.fer.zemris.ferko.domain.model.ForumPost;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link ForumRepository}. */
public class JdbcForumRepository implements ForumRepository {

  private static final RowMapper<ForumPost> MAPPER =
      (rs, rowNum) ->
          new ForumPost(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getObject("parent_id") == null ? null : rs.getLong("parent_id"),
              rs.getString("author_name"),
              rs.getString("body"),
              rs.getTimestamp("created_at").toLocalDateTime());

  private final JdbcTemplate jdbcTemplate;

  public JdbcForumRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public ForumPost save(ForumPost post) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into forum_post (course_id, parent_id, author_name, body, created_at)"
                + " values (?, ?, ?, ?, ?)",
            post.courseId(),
            post.parentId(),
            post.authorName(),
            post.body(),
            post.createdAt());
    return new ForumPost(
        id, post.courseId(), post.parentId(), post.authorName(), post.body(), post.createdAt());
  }

  @Override
  public List<ForumPost> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from forum_post where course_id = ? order by created_at", MAPPER, courseId);
  }
}
