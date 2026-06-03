package com.example.aiticketanalyser.service;

import com.example.aiticketanalyser.dto.TicketAnalysis;
import com.example.aiticketanalyser.exception.TicketAnalysisException;
import com.example.aiticketanalyser.validation.TicketValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public TicketAnalysis analyse(String ticketText) {

        // Step 1 — Validate input
        ticketValidator.validate(ticketText);
        log.info("Starting analysis for ticket of length: {}", ticketText.length());

        // Step 2 — Load prompt template
        String promptTemplate = loadPromptTemplate();

        // Step 3 — Build final prompt
        String prompt = promptTemplate.replace("{ticketText}", ticketText);

        // Step 4 — Call OpenAI via Spring AI
        String aiResponse = callOpenAi(prompt);
        log.debug("Raw AI response received, length: {}", aiResponse.length());

        // Step 5 — Parse JSON response
        return parseResponse(aiResponse);
    }

    // ── Load prompt from resources ──────────────────────────────────────────
    private String loadPromptTemplate() {
        try {
            ClassPathResource resource =
                    new ClassPathResource("prompts/ticket-analysis-prompt.st");
            return StreamUtils.copyToString(
                    resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.error("Failed to load prompt template: {}", ex.getMessage());
            throw new TicketAnalysisException("Failed to load AI prompt template.", ex);
        }
    }

    // ── Call OpenAI via Spring AI ChatClient ────────────────────────────────
    private String callOpenAi(String prompt) {
        try {
            return chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("OpenAI API call failed: {}", ex.getMessage());
            throw new TicketAnalysisException(
                    "Failed to get response from AI service. Please try again.", ex);
        }
    }

    // ── Parse AI JSON response into TicketAnalysis ──────────────────────────
    private TicketAnalysis parseResponse(String response) {
        try {
            // Clean any accidental markdown fences
            String cleaned = response
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            TicketAnalysis result = objectMapper.readValue(cleaned, TicketAnalysis.class);
            log.info("Analysis complete — priority: {}, sentiment: {}, team: {}",
                    result.getPriority(),
                    result.getSentiment(),
                    result.getSuggestedTeam());
            return result;

        } catch (Exception ex) {
            log.error("Failed to parse AI response: {}", ex.getMessage());
            throw new TicketAnalysisException(
                    "AI returned an unexpected format. Please try again.", ex);
        }
    }
}