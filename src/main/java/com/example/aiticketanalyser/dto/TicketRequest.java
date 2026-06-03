package com.example.aiticketanalyser.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {

    @NotBlank(message = "Ticket content must not be blank")
    @Size(min = 20, max = 5000,
            message = "Ticket content must be between 20 and 5000 characters")
    private String ticketText;
}