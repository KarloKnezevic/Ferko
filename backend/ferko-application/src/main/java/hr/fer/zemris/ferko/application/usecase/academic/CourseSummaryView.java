package hr.fer.zemris.ferko.application.usecase.academic;

/** Lightweight read projection of a course for listings. */
public record CourseSummaryView(
    long id, String code, String name, String semesterCode, int ects, int enrolledStudents) {}
