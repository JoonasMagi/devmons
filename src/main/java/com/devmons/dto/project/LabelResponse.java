package com.devmons.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Label.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelResponse {
    private Long id;
    private String name;
    private String color;
}
