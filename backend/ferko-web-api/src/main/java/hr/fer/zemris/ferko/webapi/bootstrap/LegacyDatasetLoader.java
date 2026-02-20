package hr.fer.zemris.ferko.webapi.bootstrap;

import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.CourseCatalogEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.EnrollmentEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.ExamEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.RawLineEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.ScheduleEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LegacyDatasetLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(LegacyDatasetLoader.class);

  private static final String COURSE_RESOURCE_PATTERN = "classpath*:bootstrap/course-isvu-data/*";
  private static final String NOVI_PODATCI_PATTERN = "classpath*:bootstrap/noviPodatci/*.txt";
  private static final Pattern EXAM_TERM_PATTERN = Pattern.compile("raspored-final-([^.]+)\\.txt");

  private final PathMatchingResourcePatternResolver resourceResolver =
      new PathMatchingResourcePatternResolver();

  private LegacyDataset cachedDataset;

  public synchronized LegacyDataset load() {
    if (cachedDataset != null) {
      return cachedDataset;
    }

    Map<String, CourseCatalogEntry> courses = loadCourses();
    List<EnrollmentEntry> enrollments = loadEnrollments();
    List<ScheduleEntry> schedules = loadSchedules();
    List<ExamEntry> exams = loadExams();
    List<RawLineEntry> rawLines = loadRawLines();

    cachedDataset = new LegacyDataset(courses, enrollments, schedules, exams, rawLines);
    LOGGER.info(
        "Loaded legacy dataset snapshot: courses={}, enrollments={}, schedules={}, exams={}, rawLines={}",
        courses.size(),
        enrollments.size(),
        schedules.size(),
        exams.size(),
        rawLines.size());
    return cachedDataset;
  }

  private Map<String, CourseCatalogEntry> loadCourses() {
    Map<String, CourseCatalogEntry> byCode = new LinkedHashMap<>();
    for (Resource resource : resolve(COURSE_RESOURCE_PATTERN)) {
      String courseCode = normalizeCourseCode(resource.getFilename());
      if (!StringUtils.hasText(courseCode)) {
        continue;
      }
      Map<String, String> rawProperties = parseSimpleProperties(resource);
      CourseCatalogEntry entry =
          new CourseCatalogEntry(
              courseCode,
              text(rawProperties.get("naziv"), "Course " + courseCode),
              text(rawProperties.get("opterecenja"), ""),
              text(rawProperties.get("opis"), ""),
              text(rawProperties.get("liter"), ""),
              text(rawProperties.get("nositelji"), ""),
              text(rawProperties.get("izvodaci"), ""));
      byCode.put(courseCode, entry);
    }
    return byCode;
  }

  private List<EnrollmentEntry> loadEnrollments() {
    List<EnrollmentEntry> entries = new ArrayList<>();
    for (String line : loadLinesByFilename("isvuUTF8.txt")) {
      if (!StringUtils.hasText(line)) {
        continue;
      }
      String[] primaryParts = line.split("##", 3);
      if (primaryParts.length < 3) {
        continue;
      }
      String[] secondaryParts = primaryParts[2].split("#", -1);
      String groupCode = secondaryParts.length > 0 ? secondaryParts[0].trim() : "";
      String studentName = secondaryParts.length > 1 ? secondaryParts[1].trim() : "";
      String courseName = secondaryParts.length > 2 ? secondaryParts[2].trim() : "";
      int yearOfStudy = parseInt(secondaryParts.length > 3 ? secondaryParts[3].trim() : "", 1);

      entries.add(
          new EnrollmentEntry(
              primaryParts[0].trim(),
              normalizeCourseCode(primaryParts[1]),
              groupCode,
              studentName,
              courseName,
              yearOfStudy,
              line));
    }
    return entries;
  }

  private List<ScheduleEntry> loadSchedules() {
    List<ScheduleEntry> entries = new ArrayList<>();
    for (String line : loadLinesByFilename("satnica.txt")) {
      if (!StringUtils.hasText(line)) {
        continue;
      }
      String[] parts = line.split("#", -1);
      if (parts.length < 7) {
        continue;
      }
      LocalDate date = parseDate(parts[0]);
      LocalTime startsAt = parseTime(parts[1]);
      if (date == null || startsAt == null) {
        continue;
      }
      entries.add(
          new ScheduleEntry(
              date,
              startsAt,
              parseInt(parts[2], 120),
              parts[3].trim(),
              parts[4].trim(),
              normalizeCourseCode(parts[5]),
              parts[6].trim()));
    }
    return entries;
  }

  private List<ExamEntry> loadExams() {
    List<ExamEntry> entries = new ArrayList<>();
    for (Resource resource : resolve(NOVI_PODATCI_PATTERN)) {
      String filename = filename(resource);
      Matcher matcher = EXAM_TERM_PATTERN.matcher(filename);
      if (!matcher.matches()) {
        continue;
      }
      String termType = matcher.group(1).toUpperCase(Locale.ROOT);
      for (String line : loadLines(resource)) {
        if (!StringUtils.hasText(line)) {
          continue;
        }
        String[] parts = line.split("\\t", -1);
        if (parts.length < 5) {
          continue;
        }
        LocalDate date = parseDate(parts[0]);
        LocalTime startsAt = parseTime(parts[1]);
        if (date == null || startsAt == null) {
          continue;
        }
        entries.add(
            new ExamEntry(
                termType,
                date,
                startsAt,
                parseInt(parts[2], 2),
                parts[3].trim(),
                normalizeCourseCode(parts[4])));
      }
    }
    return entries;
  }

  private List<RawLineEntry> loadRawLines() {
    List<RawLineEntry> rows = new ArrayList<>();
    for (Resource resource : resolve(COURSE_RESOURCE_PATTERN)) {
      appendRawLines(rows, "course-isvu-data/" + filename(resource), loadLines(resource));
    }
    for (Resource resource : resolve(NOVI_PODATCI_PATTERN)) {
      appendRawLines(rows, "noviPodatci/" + filename(resource), loadLines(resource));
    }
    return rows;
  }

  private void appendRawLines(List<RawLineEntry> target, String sourceFile, List<String> lines) {
    for (int index = 0; index < lines.size(); index++) {
      target.add(new RawLineEntry(sourceFile, index + 1, lines.get(index)));
    }
  }

  private Map<String, String> parseSimpleProperties(Resource resource) {
    Map<String, String> values = new LinkedHashMap<>();
    for (String line : loadLines(resource)) {
      if (!StringUtils.hasText(line) || line.startsWith("#")) {
        continue;
      }
      int separator = line.indexOf('=');
      if (separator <= 0) {
        continue;
      }
      String key = line.substring(0, separator).trim();
      String value = line.substring(separator + 1).trim().replace("\\n", "\n");
      values.put(key, value);
    }
    return values;
  }

  private List<String> loadLinesByFilename(String filename) {
    for (Resource resource : resolve(NOVI_PODATCI_PATTERN)) {
      if (filename(resource).equals(filename)) {
        return loadLines(resource);
      }
    }
    LOGGER.warn("Legacy dataset resource {} was not found in classpath.", filename);
    return List.of();
  }

  private List<Resource> resolve(String pattern) {
    try {
      return Arrays.stream(resourceResolver.getResources(pattern))
          .filter(Objects::nonNull)
          .filter(Resource::exists)
          .sorted(Comparator.comparing(this::filename))
          .toList();
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to resolve resources for pattern: " + pattern, ex);
    }
  }

  private List<String> loadLines(Resource resource) {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException ex) {
      throw new IllegalStateException("Failed reading resource " + filename(resource), ex);
    }
  }

  private String filename(Resource resource) {
    return text(resource.getFilename(), "unknown");
  }

  private String normalizeCourseCode(String rawCode) {
    if (!StringUtils.hasText(rawCode)) {
      return "";
    }
    return rawCode.trim().toUpperCase(Locale.ROOT);
  }

  private static LocalDate parseDate(String raw) {
    try {
      return LocalDate.parse(raw.trim());
    } catch (RuntimeException ex) {
      return null;
    }
  }

  private static LocalTime parseTime(String raw) {
    try {
      return LocalTime.parse(raw.trim());
    } catch (RuntimeException ex) {
      return null;
    }
  }

  private static int parseInt(String raw, int fallback) {
    try {
      return Integer.parseInt(raw.trim());
    } catch (RuntimeException ex) {
      return fallback;
    }
  }

  private static String text(String value, String fallback) {
    if (!StringUtils.hasText(value)) {
      return fallback;
    }
    return value.trim();
  }
}
