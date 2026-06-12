package hr.fer.zemris.ferko.application.usecase.academic;

/** Read projection of a student profile. */
public record StudentView(
    long id, String jmbag, String fullName, String studyProgram, int yearOfStudy) {}
