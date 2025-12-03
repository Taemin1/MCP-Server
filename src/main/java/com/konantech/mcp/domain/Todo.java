package com.konantech.mcp.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Todo {

    private UUID id;

    private String title;

    private String description;

    private Boolean isDone = false;

    private Integer priority = 3; // 1: 낮음, 2: 중간, 3: 높음

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public void touchUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}

