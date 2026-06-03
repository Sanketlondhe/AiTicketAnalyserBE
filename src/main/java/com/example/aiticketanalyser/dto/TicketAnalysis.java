package com.example.aiticketanalyser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketAnalysis {

    @JsonProperty("category")
    private String category;

    @JsonProperty("subcategory")
    private String subcategory;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("sentiment")
    private String sentiment;

    @JsonProperty("suggestedTeam")
    private String suggestedTeam;

    @JsonProperty("estimatedResolutionMinutes")
    private int estimatedResolutionMinutes;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("keyIssues")
    private List<String> keyIssues;
}