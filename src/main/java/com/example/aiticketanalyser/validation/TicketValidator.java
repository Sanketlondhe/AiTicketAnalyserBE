package com.example.aiticketanalyser.validation;

import org.springframework.stereotype.Component;

@Component
public class TicketValidator {

    private static final int MIN_LENGTH = 20;
    private static final int MAX_LENGTH = 5000;

    public void validate(String ticketText) {

        if (ticketText == null || ticketText.isBlank()) {
            throw new IllegalArgumentException("Ticket content must not be blank.");
        }

        String trimmed = ticketText.trim();

        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Ticket content is too short. Minimum " + MIN_LENGTH + " characters required.");
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Ticket content is too long. Maximum " + MAX_LENGTH + " characters allowed.");
        }
    }
}
