package hr.fer.zemris.ferko.application.usecase.academic;

import java.util.List;

/** Read projection of a course enrollment, including the groups the student is assigned to. */
public record EnrollmentView(
    long id,
    long studentId,
    String studentJmbag,
    String studentFullName,
    long courseId,
    String status,
    List<String> groupCodes) {}
