package com.dentalwhisper.backend.patient;

import java.time.Instant;

public record Patient(Long id, String firstName, String lastName, Instant addedAt) {
}
