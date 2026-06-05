package com.ticket.analyser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.analyser.dto.TicketAnalysis;
import com.ticket.analyser.exception.TicketAnalysisException;
import com.ticket.analyser.validation.TicketValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketAnalysisService {

    private final ChatClient chatClient;
    private final TicketValidator ticketValidator;
    private final ObjectMapper objectMapper;

    // ── Cached once at startup — not reloaded every request ─────────────────
    private String promptTemplate;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource =
                new ClassPathResource("prompts/ticket-analysis-prompt.st");
            promptTemplate = StreamUtils.copyToString(
                resource.getInputStream(), StandardCharsets.UTF_8);
            log.info("Prompt template loaded and cached successfully.");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load prompt template on startup.", ex);
        }
    }

    public TicketAnalysis analyse(String ticketText) {
        ticketValidator.validate(ticketText);
        log.info("Analysing ticket, length: {}", ticketText.length());

        String prompt = promptTemplate.replace("{ticketText}", ticketText);
        String aiResponse = callOpenAi(prompt);
        return parseResponse(aiResponse);
    }

    private String callOpenAi(String prompt) {
        try {
            long start = System.currentTimeMillis();
            String response = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.info("OpenAI responded in {}ms", System.currentTimeMillis() - start);
            return response;
        } catch (Exception ex) {
            log.error("OpenAI API call failed: {}", ex.getMessage());
            throw new TicketAnalysisException(
                "Failed to get response from AI service. Please try again.", ex);
        }
    }

    private TicketAnalysis parseResponse(String response) {
        try {
            String cleaned = response
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            TicketAnalysis result = objectMapper.readValue(cleaned, TicketAnalysis.class);
            log.info("Analysis done — priority: {}, sentiment: {}",
                    result.getPriority(), result.getSentiment());
            return result;
        } catch (Exception ex) {
            log.error("Failed to parse AI response: {}", ex.getMessage());
            throw new TicketAnalysisException(
                "AI returned unexpected format. Please try again.", ex);
        }
    }
}
