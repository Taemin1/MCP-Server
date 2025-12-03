package com.konantech.mcp.domain;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolLogic {

    private UUID logicId;
    private UUID toolId;

    private String endpointUrl;
    private String httpMethod;
    private String requestTemplate;
}
