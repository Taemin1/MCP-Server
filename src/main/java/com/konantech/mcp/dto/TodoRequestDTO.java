package com.konantech.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

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

