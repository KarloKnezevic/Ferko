package hr.fer.zemris.ferko.domain.model;

/** Assignment of an enrollment to a concrete group. */
public record GroupMembership(long id, long enrollmentId, long groupId) {}
