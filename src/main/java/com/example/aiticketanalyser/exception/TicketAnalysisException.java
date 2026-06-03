package com.example.aiticketanalyser.exception;

public class TicketAnalysisException extends RuntimeException {

    public TicketAnalysisException(String message) {
        super(message);
    }

    public TicketAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
