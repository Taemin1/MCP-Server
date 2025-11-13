package com.konantech.mcp.dto;

import com.konantech.mcp.enums.Platform;
import com.konantech.mcp.enums.Role;

public record UserRequestDTO(String username, Role role, Platform platform) {
}
