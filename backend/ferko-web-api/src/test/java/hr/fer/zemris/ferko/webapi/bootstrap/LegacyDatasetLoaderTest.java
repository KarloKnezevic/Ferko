package hr.fer.zemris.ferko.webapi.bootstrap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LegacyDatasetLoaderTest {

  @Test
  void loadsPackagedLegacyDatasetResources() {
    LegacyDatasetLoader loader = new LegacyDatasetLoader();
    LegacyDataset dataset = loader.load();

    assertFalse(dataset.courses().isEmpty());
    assertFalse(dataset.enrollments().isEmpty());
    assertFalse(dataset.schedules().isEmpty());
    assertFalse(dataset.exams().isEmpty());
    assertFalse(dataset.rawLines().isEmpty());

    assertTrue(dataset.courses().containsKey("31489"));
  }
}
