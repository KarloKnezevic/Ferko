package hr.fer.zemris.ferko.webapi.bootstrap;

import hr.fer.zemris.ferko.webapi.portal.FerkoPortalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class LegacyPortalBootstrapInitializer implements ApplicationRunner {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LegacyPortalBootstrapInitializer.class);

  private final FerkoPortalService portalService;
  private final LegacyDatasetLoader datasetLoader;
  private final boolean enabled;
  private final int maxCourses;
  private final int maxStudents;
  private final int maxScheduleEntries;
  private final int maxExamEntries;

  public LegacyPortalBootstrapInitializer(
      FerkoPortalService portalService,
      LegacyDatasetLoader datasetLoader,
      @Value("${ferko.portal.bootstrap.enabled:true}") boolean enabled,
      @Value("${ferko.portal.bootstrap.max-courses:18}") int maxCourses,
      @Value("${ferko.portal.bootstrap.max-students:260}") int maxStudents,
      @Value("${ferko.portal.bootstrap.max-schedule-entries:220}") int maxScheduleEntries,
      @Value("${ferko.portal.bootstrap.max-exam-entries:80}") int maxExamEntries) {
    this.portalService = portalService;
    this.datasetLoader = datasetLoader;
    this.enabled = enabled;
    this.maxCourses = maxCourses;
    this.maxStudents = maxStudents;
    this.maxScheduleEntries = maxScheduleEntries;
    this.maxExamEntries = maxExamEntries;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!enabled) {
      LOGGER.info("Portal legacy bootstrap is disabled.");
      return;
    }

    portalService.mergeLegacyBootstrapData(
        datasetLoader.load(), maxCourses, maxStudents, maxScheduleEntries, maxExamEntries);
    LOGGER.info("Portal state enriched with legacy bootstrap dataset.");
  }
}
