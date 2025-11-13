// File: src/test/java/com/konantech/mcp/McpApplicationTests.java
package com.konantech.mcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = McpApplication.class)
class McpApplicationTests {

    @Test
    void contextLoads() {
        // simple sanity check that the Spring context starts
    }
}
