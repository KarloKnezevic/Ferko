package hr.fer.zemris.ferko.application.usecase.academic;

import java.util.List;

/** Read model for an application user shown in the admin console. */
public record AppUserView(
    long id, String username, String fullName, String email, boolean active, List<String> roles) {}
