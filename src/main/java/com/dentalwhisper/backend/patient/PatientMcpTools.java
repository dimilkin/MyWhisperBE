package com.dentalwhisper.backend.patient;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class PatientMcpTools {

    private static final Logger log = LoggerFactory.getLogger(PatientMcpTools.class);

    private final PatientService patientService;

    public PatientMcpTools(PatientService patientService) {
        this.patientService = patientService;
    }

    @McpTool(
            name = "create_patient",
            description = "Create a new dental patient record with a first and last name.",
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(
                    title = "Create Patient",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = false,
                    openWorldHint = false))
    public Patient createPatient(
            @McpToolParam(description = "Patient's first name", required = true) String firstName,
            @McpToolParam(description = "Patient's last name", required = true) String lastName) {
        Patient patient = patientService.addPatient(new PatientRequest(firstName, lastName));
        log.info("MCP tool call create_patient: firstName={}, lastName={}, id={}", firstName, lastName, patient.id());
        return patient;
    }

    @McpTool(
            name = "get_patient_by_name",
            description = "Look up dental patients by first and/or last name. Each result includes the "
                    + "date and time the patient was registered (addedAt).",
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(
                    title = "Get Patient By Name",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public List<Patient> getPatientByName(
            @McpToolParam(description = "Full or partial patient name to search for", required = true) String name) {
        List<Patient> results = patientService.findByName(name);
        log.info("MCP tool call get_patient_by_name: name={}, results={}", name, results.size());
        return results;
    }
}
