package com.dentalwhisper.backend.patient;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final PatientDatabase patientDatabase;

    public PatientService(PatientDatabase patientDatabase) {
        this.patientDatabase = patientDatabase;
    }

    public List<Patient> getAllPatients() {
        return patientDatabase.findAll();
    }

    public List<Patient> findByName(String name) {
        return patientDatabase.findByName(name);
    }

    public Patient addPatient(PatientRequest request) {
        return patientDatabase.save(request.firstName(), request.lastName());
    }
}
