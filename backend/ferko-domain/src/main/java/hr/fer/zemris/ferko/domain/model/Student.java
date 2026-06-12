package hr.fer.zemris.ferko.domain.model;

/** A student profile linked to an {@link AppUser}. */
public record Student(long id, long userId, String jmbag, String studyProgram, int yearOfStudy) {}
