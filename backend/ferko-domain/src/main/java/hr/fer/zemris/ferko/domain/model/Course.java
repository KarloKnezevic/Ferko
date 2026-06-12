package hr.fer.zemris.ferko.domain.model;

/** A course offered in a given semester. */
public record Course(
    long id,
    String code,
    String name,
    String semesterCode,
    int ects,
    String description,
    String literature) {}
