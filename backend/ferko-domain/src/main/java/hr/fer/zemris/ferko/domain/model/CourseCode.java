package hr.fer.zemris.ferko.domain.model;

public record CourseCode(String value) {

  public CourseCode {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Course code must not be blank.");
    }
  }
}
