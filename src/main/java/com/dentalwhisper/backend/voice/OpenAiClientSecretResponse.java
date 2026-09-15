package com.dentalwhisper.backend.voice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiClientSecretResponse(String value, @JsonProperty("expires_at") long expiresAt) {
}
