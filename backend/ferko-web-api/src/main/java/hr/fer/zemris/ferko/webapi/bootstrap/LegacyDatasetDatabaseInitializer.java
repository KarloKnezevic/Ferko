package hr.fer.zemris.ferko.webapi.bootstrap;

import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.CourseCatalogEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.EnrollmentEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.ExamEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.RawLineEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.ScheduleEntry;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class LegacyDatasetDatabaseInitializer implements ApplicationRunner {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LegacyDatasetDatabaseInitializer.class);
  private static final int BATCH_SIZE = 1000;

  private final LegacyDatasetLoader datasetLoader;
  private final JdbcTemplate jdbcTemplate;
  private final boolean enabled;

  public LegacyDatasetDatabaseInitializer(
      LegacyDatasetLoader datasetLoader,
      JdbcTemplate jdbcTemplate,
      @Value("${ferko.bootstrap.legacy.enabled:true}") boolean enabled) {
    this.datasetLoader = datasetLoader;
    this.jdbcTemplate = jdbcTemplate;
    this.enabled = enabled;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!enabled) {
      LOGGER.info("Legacy dataset bootstrap is disabled.");
      return;
    }

    if (countRows("legacy_bootstrap_raw_line") > 0) {
      LOGGER.info("Legacy dataset tables already populated. Skipping bootstrap persistence.");
      return;
    }

    LegacyDataset dataset = datasetLoader.load();
    insertCourses(dataset.courses().values().stream().toList());
    insertEnrollments(dataset.enrollments());
    insertSchedules(dataset.schedules());
    insertExams(dataset.exams());
    insertRawLines(dataset.rawLines());

    LOGGER.info(
        "Persisted legacy dataset snapshot to PostgreSQL/H2: courses={}, enrollments={}, schedules={}, exams={}, rawLines={}",
        dataset.courses().size(),
        dataset.enrollments().size(),
        dataset.schedules().size(),
        dataset.exams().size(),
        dataset.rawLines().size());
  }

  private void insertCourses(List<CourseCatalogEntry> courses) {
    String sql =
        """
        insert into legacy_bootstrap_course (
            course_code,
            title,
            workload_raw,
            description,
            literature,
            leaders,
            instructors
        ) values (?, ?, ?, ?, ?, ?, ?)
        """;
    batchInsert(
        sql,
        courses,
        (ps, row) -> {
          ps.setString(1, truncate(row.courseCode(), 32));
          ps.setString(2, truncate(row.title(), 512));
          ps.setString(3, truncate(row.workloadRaw(), 256));
          ps.setString(4, row.description());
          ps.setString(5, row.literature());
          ps.setString(6, row.leaders());
          ps.setString(7, row.instructors());
        });
  }

  private void insertEnrollments(List<EnrollmentEntry> enrollments) {
    String sql =
        """
        insert into legacy_bootstrap_enrollment (
            jmbag,
            course_code,
            group_code,
            student_name,
            course_name,
            year_of_study,
            source_line
        ) values (?, ?, ?, ?, ?, ?, ?)
        """;
    batchInsert(
        sql,
        enrollments,
        (ps, row) -> {
          ps.setString(1, truncate(row.jmbag(), 16));
          ps.setString(2, truncate(row.courseCode(), 32));
          ps.setString(3, truncate(row.groupCode(), 64));
          ps.setString(4, truncate(row.studentName(), 255));
          ps.setString(5, truncate(row.courseName(), 512));
          ps.setInt(6, row.yearOfStudy());
          ps.setString(7, truncate(row.sourceLine(), 2048));
        });
  }

  private void insertSchedules(List<ScheduleEntry> schedules) {
    String sql =
        """
        insert into legacy_bootstrap_schedule (
            schedule_date,
            starts_at,
            duration_minutes,
            institution,
            room,
            course_code,
            groups_text
        ) values (?, ?, ?, ?, ?, ?, ?)
        """;
    batchInsert(
        sql,
        schedules,
        (ps, row) -> {
          ps.setDate(1, Date.valueOf(row.date()));
          ps.setTime(2, Time.valueOf(row.startsAt()));
          ps.setInt(3, row.durationMinutes());
          ps.setString(4, truncate(row.institution(), 128));
          ps.setString(5, truncate(row.room(), 128));
          ps.setString(6, truncate(row.courseCode(), 32));
          ps.setString(7, truncate(row.groupsText(), 1024));
        });
  }

  private void insertExams(List<ExamEntry> exams) {
    String sql =
        """
        insert into legacy_bootstrap_exam (
            term_type,
            exam_date,
            starts_at,
            duration_hours,
            course_name,
            course_code
        ) values (?, ?, ?, ?, ?, ?)
        """;
    batchInsert(
        sql,
        exams,
        (ps, row) -> {
          ps.setString(1, truncate(row.termType(), 32));
          ps.setDate(2, Date.valueOf(row.date()));
          ps.setTime(3, Time.valueOf(row.startsAt()));
          ps.setInt(4, row.durationHours());
          ps.setString(5, truncate(row.courseName(), 512));
          ps.setString(6, truncate(row.courseCode(), 32));
        });
  }

  private void insertRawLines(List<RawLineEntry> rawLines) {
    String sql =
        """
        insert into legacy_bootstrap_raw_line (
            source_file,
            line_no,
            line_text
        ) values (?, ?, ?)
        """;
    batchInsert(
        sql,
        rawLines,
        (ps, row) -> {
          ps.setString(1, truncate(row.sourceFile(), 255));
          ps.setInt(2, row.lineNo());
          ps.setString(3, row.lineText());
        });
  }

  private int countRows(String tableName) {
    Integer count = jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class);
    return count == null ? 0 : count;
  }

  private <T> void batchInsert(
      String sql, List<T> rows, ParameterizedPreparedStatementSetter<T> setter) {
    if (rows.isEmpty()) {
      return;
    }
    jdbcTemplate.batchUpdate(sql, rows, BATCH_SIZE, setter);
  }

  private static String truncate(String value, int maxLength) {
    if (value == null || value.length() <= maxLength) {
      return value == null ? "" : value;
    }
    return value.substring(0, maxLength);
  }
}
