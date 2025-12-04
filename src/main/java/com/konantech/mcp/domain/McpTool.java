package com.konantech.mcp.domain;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpTool {

    private UUID toolId;
    private String name;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private List<McpToolParam> params;
    private McpToolSchema schema;
    private McpToolLogic logic;
}
