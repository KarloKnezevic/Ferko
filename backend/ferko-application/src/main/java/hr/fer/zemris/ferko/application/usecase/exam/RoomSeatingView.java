package hr.fer.zemris.ferko.application.usecase.exam;

import java.util.List;

/** Seating of one room for an exam: the room, its capacity and the assigned students. */
public record RoomSeatingView(
    long roomId, String roomCode, int capacity, int assignedStudents, List<ExamSeatView> seats) {}
