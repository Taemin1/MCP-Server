package com.konantech.mcp.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoRequestDTO {
    private UUID id;
    private String title;
    private String description;
    private Boolean isDone;
    private Integer priority;
}
