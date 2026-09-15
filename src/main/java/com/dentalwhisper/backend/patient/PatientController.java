package com.dentalwhisper.backend.patient;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<Patient> getPatients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/search")
    public List<Patient> searchPatients(@RequestParam String name) {
        List<Patient> results = patientService.findByName(name);
        log.info("Voice tool call get_patient_by_name: name={}, results={}", name, results.size());
        return results;
    }

    @PostMapping
    public Patient addPatient(@Valid @RequestBody PatientRequest request) {
        return patientService.addPatient(request);
    }
}
