package hr.fer.zemris.ferko.application.usecase.academic;

/**
 * Snapshot of synchronised academic data (counts). The live ISVU import runs idempotently on deploy
 * via the data seeder; this view lets administrators verify what is currently loaded.
 */
public record SyncStatusView(long semesters, long courses, long students, long rooms) {}
