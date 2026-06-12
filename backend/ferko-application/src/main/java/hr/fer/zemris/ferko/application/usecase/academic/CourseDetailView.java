package hr.fer.zemris.ferko.application.usecase.academic;

import java.util.List;

/** Full read projection of a course with its staff and groups. */
public record CourseDetailView(
    long id,
    String code,
    String name,
    String semesterCode,
    int ects,
    String description,
    String literature,
    int enrolledStudents,
    List<CourseStaffView> staff,
    List<StudentGroupView> groups) {}
