package hr.fer.zemris.ferko.application.usecase.profile;

import java.util.List;

/**
 * The signed-in user's personal data ("Osobni podaci"). Student-specific fields ({@code jmbag},
 * {@code studyProgram}, {@code yearOfStudy}) are {@code null}/0 for non-students.
 */
public record MyProfileView(
    String username,
    String fullName,
    String email,
    List<String> roles,
    String jmbag,
    String studyProgram,
    int yearOfStudy) {}
