package com.dentalwhisper.backend.patient;

import jakarta.validation.constraints.NotBlank;

public record PatientRequest(@NotBlank String firstName, @NotBlank String lastName) {
}
