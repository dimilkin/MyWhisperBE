package com.dentalwhisper.backend.voice;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class VoiceSessionService {

    private static final String CLIENT_SECRETS_URL = "https://api.openai.com/v1/realtime/client_secrets";
    private static final String MCP_SERVER_LABEL = "dentalwhisper_patients";

    private final RestClient restClient;
    private final String model;
    private final String mcpServerPublicUrl;

    public VoiceSessionService(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.realtime.model}") String model,
            @Value("${mcp.server.public-url}") String mcpServerPublicUrl) {
        this.model = model;
        this.mcpServerPublicUrl = mcpServerPublicUrl;
        this.restClient = RestClient.builder()
                .baseUrl(CLIENT_SECRETS_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public VoiceSessionResponse createEphemeralSession() {
        Map<String, Object> requestBody = Map.of(
                "session", Map.of(
                        "type", "realtime",
                        "model", model,
                        "instructions",
                        "You are a friendly front-desk assistant for a dental office. You can create new patient "
                                + "records and look up existing patients by name, including the date and time "
                                + "they were registered. As soon as the conversation starts, greet the caller "
                                + "briefly and ask how you can help — don't wait for them to speak first.",
                        "audio", Map.of("output", Map.of("voice", "alloy")),
                        "tools", buildTools(),
                        "tool_choice", "auto"));

        OpenAiClientSecretResponse response = restClient.post()
                .body(requestBody)
                .retrieve()
                .body(OpenAiClientSecretResponse.class);

        return new VoiceSessionResponse(response.value(), model);
    }

    private List<Map<String, Object>> buildTools() {
        if (mcpServerPublicUrl.isBlank()) {
            return List.of();
        }
        return List.of(Map.of(
                "type", "mcp",
                "server_label", MCP_SERVER_LABEL,
                "server_url", mcpServerPublicUrl + "/mcp",
                "allowed_tools", List.of("create_patient", "get_patient_by_name"),
                "require_approval", "never"));
    }
}
