package hr.fer.zemris.ferko.application.usecase.schedule;

/** Read model for one weekly timetable slot of a course; times formatted as {@code HH:mm}. */
public record ScheduleSlotView(
    long id,
    String dayOfWeek,
    String startsAt,
    String endsAt,
    String type,
    String groupCode,
    String roomCode,
    String instructor) {}
