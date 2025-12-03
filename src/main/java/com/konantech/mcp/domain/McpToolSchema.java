package com.konantech.mcp.domain;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolSchema {

    private UUID schemaId;
    private UUID toolId;

    private String schemaJson;
}
