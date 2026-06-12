package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDate;

/** An academic semester, identified by its code (e.g. {@code 2024Z}). */
public record Semester(
    String code,
    String academicYear,
    String term,
    LocalDate startsOn,
    LocalDate endsOn,
    boolean active) {}
