package com.ticket.analyser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.aiticketanalyser.dto.TicketAnalysis;
import com.example.aiticketanalyser.exception.TicketAnalysisException;
import com.example.aiticketanalyser.validation.TicketValidator;
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

            // Collect full streaming response
            StringBuilder responseBuilder = new StringBuilder();

            chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content()
                    .doOnNext(responseBuilder::append)
                    .blockLast();

            log.info("OpenAI streamed response in {}ms",
                    System.currentTimeMillis() - start);

            return parseResponse(responseBuilder.toString());

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
