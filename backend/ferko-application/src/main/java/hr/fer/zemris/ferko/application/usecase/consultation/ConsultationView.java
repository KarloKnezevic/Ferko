package hr.fer.zemris.ferko.application.usecase.consultation;

/** Read model for an office-hours slot; times formatted as {@code HH:mm}. */
public record ConsultationView(
    long id,
    long courseId,
    String staffName,
    String dayOfWeek,
    String startsAt,
    String endsAt,
    String location) {}
