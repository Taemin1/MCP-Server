package com.konantech.mcp.domain;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolParam {

    private UUID paramId;
    private UUID toolId;

    private String name;
    private String type;
    private String description;
    private boolean required;
}
