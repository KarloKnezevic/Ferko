package hr.fer.zemris.ferko.application.usecase.academic;

/** Read projection of a course enrollment. */
public record EnrollmentView(
    long id,
    long studentId,
    String studentJmbag,
    String studentFullName,
    long courseId,
    String status) {}
