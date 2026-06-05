package com.example.aiticketanalyser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.aiticketanalyser.dto.TicketAnalysis;
import com.example.aiticketanalyser.exception.TicketAnalysisException;
import com.example.aiticketanalyser.validation.TicketValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TicketAnalysisService {

    private static final Logger log =
            LoggerFactory.getLogger(TicketAnalysisService.class);

    private final ChatClient chatClient;
    private final TicketValidator ticketValidator;
    private final ObjectMapper objectMapper;

    private String promptTemplate;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource =
                new ClassPathResource("prompts/ticket-analysis-prompt.st");
            promptTemplate = StreamUtils.copyToString(
                resource.getInputStream(), StandardCharsets.UTF_8);
            log.info("Prompt template cached at startup.");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load prompt template.", ex);
        }
    }

    public TicketAnalysis analyse(String ticketText) {
        ticketValidator.validate(ticketText);
        String prompt = promptTemplate.replace("{ticketText}", ticketText);
        try {
            long start = System.currentTimeMillis();
            String response = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.info("OpenAI responded in {}ms", System.currentTimeMillis() - start);
            return parseResponse(response);
        } catch (Exception ex) {
            log.error("OpenAI call failed: {}", ex.getMessage());
            throw new TicketAnalysisException(
                "AI service failed. Please try again.", ex);
        }
    }

    private TicketAnalysis parseResponse(String response) {
        try {
            String cleaned = response
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            return objectMapper.readValue(cleaned, TicketAnalysis.class);
        } catch (Exception ex) {
            log.error("Parse failed: {}", ex.getMessage());
            throw new TicketAnalysisException(
                "AI returned unexpected format.", ex);
        }
    }
}
