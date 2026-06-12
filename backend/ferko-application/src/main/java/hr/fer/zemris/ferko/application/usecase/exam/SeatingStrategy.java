package hr.fer.zemris.ferko.application.usecase.exam;

/** Seating strategies offered to staff (mirrors the FERKO buttons plus the genetic optimiser). */
public enum SeatingStrategy {
  GENETIC,
  SORTED_GREEDY,
  SORTED_PROPORTIONAL,
  RANDOM_GREEDY,
  RANDOM_PROPORTIONAL
}
