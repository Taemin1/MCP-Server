package com.konantech.mcp.dto;

import com.konantech.mcp.domain.Todo;

import java.time.LocalDateTime;
import java.util.UUID;

public record TodoResponseDTO(
    UUID id,
    String title,
    String description,
    Boolean isDone,
    Integer priority,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TodoResponseDTO from(Todo todo) {
        return new TodoResponseDTO(
            todo.getId() != null ? todo.getId() : null,
            todo.getTitle(),
            todo.getDescription(),
            todo.getIsDone(),
            todo.getPriority(),
            todo.getCreatedAt(),
            todo.getUpdatedAt()
        );
    }
}

