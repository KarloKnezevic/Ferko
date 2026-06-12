package hr.fer.zemris.ferko.application.usecase.academic;

/** Read projection of a room. */
public record RoomView(
    long id,
    String code,
    String building,
    int capacity,
    int requiredAssistants,
    boolean hasComputers) {}
