package hr.fer.zemris.ferko.application.usecase.academic;

/** Read projection of a lecture/lab group. */
public record StudentGroupView(
    long id, String groupCode, String type, String category, int capacity) {}
