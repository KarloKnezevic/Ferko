package hr.fer.zemris.ferko.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CourseCodeTest {

  @Test
  void rejectsBlankValue() {
    assertThrows(IllegalArgumentException.class, () -> new CourseCode(" "));
  }
}
