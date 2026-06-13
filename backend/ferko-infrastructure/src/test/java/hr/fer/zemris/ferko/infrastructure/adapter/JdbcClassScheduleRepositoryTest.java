package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.GroupType;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class JdbcClassScheduleRepositoryTest {

  private JdbcClassScheduleRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:sched_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcClassScheduleRepository(jdbcTemplate);
  }

  @Test
  void savesAndReadsBackWeeklySlots() {
    ClassSchedule lecture =
        repository.save(
            new ClassSchedule(
                0L,
                100L,
                null,
                GroupType.LECTURE,
                null,
                DayOfWeek.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                "Marko Predavač"));
    repository.save(
        new ClassSchedule(
            0L,
            100L,
            7L,
            GroupType.LAB,
            5L,
            DayOfWeek.WEDNESDAY,
            LocalTime.of(14, 0),
            LocalTime.of(16, 0),
            "Iva Asistent"));

    assertTrue(lecture.id() > 0);

    List<ClassSchedule> slots = repository.findByCourse(100L);
    assertEquals(2, slots.size());

    ClassSchedule first = slots.get(0);
    assertEquals(DayOfWeek.MONDAY, first.dayOfWeek());
    assertEquals(LocalTime.of(8, 0), first.startsAt());
    assertEquals(GroupType.LECTURE, first.type());
    assertEquals("Marko Predavač", first.instructor());

    assertTrue(repository.findByCourse(999L).isEmpty());
  }
}
