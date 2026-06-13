package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.RepositoryFileRepository;
import hr.fer.zemris.ferko.domain.model.RepositoryFile;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link RepositoryFileRepository}. */
public class JdbcRepositoryFileRepository implements RepositoryFileRepository {

  private static final RowMapper<RepositoryFile> MAPPER =
      (rs, rowNum) ->
          new RepositoryFile(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getString("filename"),
              rs.getString("content_type"),
              rs.getLong("size_bytes"),
              rs.getString("storage_key"),
              rs.getString("uploaded_by"),
              rs.getTimestamp("uploaded_at").toLocalDateTime());

  private final JdbcTemplate jdbcTemplate;

  public JdbcRepositoryFileRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public RepositoryFile save(RepositoryFile file) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into repozitorij_datoteka"
                + " (course_id, filename, content_type, size_bytes, storage_key, uploaded_by,"
                + " uploaded_at) values (?, ?, ?, ?, ?, ?, ?)",
            file.courseId(),
            file.filename(),
            file.contentType(),
            file.sizeBytes(),
            file.storageKey(),
            file.uploadedBy(),
            file.uploadedAt());
    return new RepositoryFile(
        id,
        file.courseId(),
        file.filename(),
        file.contentType(),
        file.sizeBytes(),
        file.storageKey(),
        file.uploadedBy(),
        file.uploadedAt());
  }

  @Override
  public List<RepositoryFile> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from repozitorij_datoteka where course_id = ? order by uploaded_at desc",
        MAPPER,
        courseId);
  }

  @Override
  public Optional<RepositoryFile> findById(long id) {
    return jdbcTemplate
        .query("select * from repozitorij_datoteka where id = ?", MAPPER, id)
        .stream()
        .findFirst();
  }
}
