package hr.fer.zemris.ferko.application.usecase.academic;

import java.time.LocalDate;

/** Read projection of a semester. */
public record SemesterView(
    String code,
    String academicYear,
    String term,
    LocalDate startsOn,
    LocalDate endsOn,
    boolean active) {}
