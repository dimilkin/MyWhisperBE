package com.dentalwhisper.backend.patient;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

@Component
public class PatientDatabase {

    private final List<Patient> patients = new CopyOnWriteArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public PatientDatabase() {
        save("John", "Doe");
        save("Jane", "Smith");
    }

    public List<Patient> findAll() {
        return List.copyOf(patients);
    }

    public List<Patient> findByName(String query) {
        String needle = query.strip().toLowerCase();
        return patients.stream()
                .filter(p -> p.firstName().toLowerCase().contains(needle) || p.lastName().toLowerCase().contains(needle))
                .toList();
    }

    public Patient save(String firstName, String lastName) {
        Patient patient = new Patient(idGenerator.incrementAndGet(), firstName, lastName, Instant.now());
        patients.add(patient);
        return patient;
    }
}
