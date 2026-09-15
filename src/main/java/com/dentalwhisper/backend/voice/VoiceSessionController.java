package com.dentalwhisper.backend.voice;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/voice")
public class VoiceSessionController {

    private final VoiceSessionService voiceSessionService;

    public VoiceSessionController(VoiceSessionService voiceSessionService) {
        this.voiceSessionService = voiceSessionService;
    }

    @PostMapping("/session")
    public VoiceSessionResponse createSession() {
        return voiceSessionService.createEphemeralSession();
    }
}
