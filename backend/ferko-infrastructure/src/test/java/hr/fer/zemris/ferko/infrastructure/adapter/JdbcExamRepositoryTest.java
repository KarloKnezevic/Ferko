package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamKind;
import hr.fer.zemris.ferko.domain.model.ExamRegistration;
import hr.fer.zemris.ferko.domain.model.ExamRoom;
import hr.fer.zemris.ferko.domain.model.ExamSeat;
import hr.fer.zemris.ferko.domain.model.ExamVisibility;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class JdbcExamRepositoryTest {

  private JdbcExamRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:exam_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcExamRepository(jdbcTemplate);
  }

  @Test
  void persistsExamLifecycleWithNullableFields() {
    Exam saved =
        repository.save(
            new Exam(
                0L,
                100L,
                "Prvi međuispit",
                "MI1",
                ExamKind.MEDJUISPIT,
                LocalDateTime.parse("2025-11-15T12:00:00"),
                90,
                20.0,
                1,
                ExamVisibility.ALWAYS,
                false,
                null,
                false));
    assertTrue(saved.id() > 0);
    assertNull(saved.prerequisiteFlagId());
    assertEquals(1, repository.findByCourse(100L).size());

    // Unscheduled exam (null startsAt) is allowed.
    Exam draft =
        repository.save(
            new Exam(
                0L,
                100L,
                "Završni",
                "ZI",
                ExamKind.ZAVRSNI,
                null,
                120,
                40.0,
                2,
                ExamVisibility.WHEN_DATA,
                false,
                null,
                false));
    assertNull(repository.findById(draft.id()).orElseThrow().startsAt());

    repository.markPublished(saved.id(), true);
    assertTrue(repository.findById(saved.id()).orElseThrow().published());
    assertFalse(repository.findById(draft.id()).orElseThrow().published());
  }

  @Test
  void managesRegistrationsRoomsAndSeating() {
    Exam exam =
        repository.save(
            new Exam(
                0L,
                100L,
                "MI",
                "MI",
                ExamKind.MEDJUISPIT,
                null,
                90,
                20.0,
                1,
                ExamVisibility.ALWAYS,
                false,
                null,
                false));
    long examId = exam.id();

    repository.addRegistration(
        new ExamRegistration(0L, examId, 501L, LocalDateTime.now(), "REGISTERED"));
    repository.addRegistration(
        new ExamRegistration(0L, examId, 502L, LocalDateTime.now(), "REGISTERED"));
    assertEquals(2, repository.findRegistrations(examId).size());

    ExamRoom room = repository.addRoom(new ExamRoom(0L, examId, 9L, 100, 3, true));
    assertTrue(room.id() > 0);
    assertEquals(1, repository.findRooms(examId).size());

    repository.replaceSeats(
        examId,
        List.of(
            new ExamSeat(0L, examId, 501L, 9L, 1, "A"),
            new ExamSeat(0L, examId, 502L, 9L, 2, "B")));
    assertEquals(2, repository.findSeats(examId).size());

    // Re-scheduling replaces the previous seating.
    repository.replaceSeats(examId, List.of(new ExamSeat(0L, examId, 501L, 9L, 1, null)));
    List<ExamSeat> seats = repository.findSeats(examId);
    assertEquals(1, seats.size());
    assertNull(seats.get(0).testGroup());
  }
}
