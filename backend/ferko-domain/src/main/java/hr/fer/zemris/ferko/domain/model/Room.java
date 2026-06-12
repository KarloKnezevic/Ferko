package hr.fer.zemris.ferko.domain.model;

/** A physical room usable for lectures, labs and assessments. */
public record Room(
    long id,
    String code,
    String building,
    int capacity,
    int requiredAssistants,
    boolean hasComputers) {}
