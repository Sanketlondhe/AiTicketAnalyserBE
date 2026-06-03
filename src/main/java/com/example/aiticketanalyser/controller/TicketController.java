package com.example.aiticketanalyser.controller;

import com.example.aiticketanalyser.dto.TicketAnalysis;
import com.example.aiticketanalyser.dto.TicketRequest;
import com.example.aiticketanalyser.service.TicketAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketAnalysisService ticketAnalysisService;

    // ── POST /api/tickets/analyse ───────────────────────────────────────────
    @PostMapping("/analyse")
    public ResponseEntity<TicketAnalysis> analyseTicket(
            @Valid @RequestBody TicketRequest request) {

        log.info("Received ticket analysis request, content length: {}",
                request.getTicketText().length());

        TicketAnalysis result = ticketAnalysisService.analyse(request.getTicketText());
        return ResponseEntity.ok(result);
    }

    // ── GET /api/tickets/health ─────────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ai-support-ticket-analyser"
        ));
    }
}
