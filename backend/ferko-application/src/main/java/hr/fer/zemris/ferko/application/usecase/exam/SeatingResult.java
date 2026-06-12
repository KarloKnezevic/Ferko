package hr.fer.zemris.ferko.application.usecase.exam;

import java.util.List;

/**
 * Outcome of running the seating scheduler for an exam.
 *
 * @param strategy strategy used (e.g. {@code GENETIC}, {@code SORTED_PROPORTIONAL})
 * @param seatedStudents number of students placed
 * @param overCapacityPenalty residual over-capacity penalty (0 = all rooms within capacity)
 * @param feasible whether the seating respects all room capacities
 * @param penaltyHistory convergence samples (for genetic runs; empty otherwise)
 * @param rooms per-room seating
 */
public record SeatingResult(
    String strategy,
    int seatedStudents,
    double overCapacityPenalty,
    boolean feasible,
    List<Double> penaltyHistory,
    List<RoomSeatingView> rooms) {}
