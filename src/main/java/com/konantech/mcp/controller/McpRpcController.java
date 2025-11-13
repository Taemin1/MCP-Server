package com.konantech.mcp.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpRpcController {

    private final ToolCallbackProvider toolCallbackProvider;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonRpcResponse<?> message(
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestBody JsonRpcRequest req) {

        if (req == null || req.method == null) {
            return JsonRpcResponse.error(req != null ? req.id : null, -32600, "Invalid Request");
        }

        switch (req.method) {
            case "tools/list":
                return toolsList(req.id);
            case "tools/call":
                return toolsCall(req.id, objectMapper.convertValue(req.params, ToolCallParams.class));
            default:
                return JsonRpcResponse.error(req.id, -32601, "Method not found: " + req.method);
        }
    }

    private JsonRpcResponse<ToolListResult> toolsList(Object id) {
        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
        List<ToolInfo> tools = Arrays.stream(callbacks)
                .map(this::toToolInfo)
                .collect(Collectors.toList());

        ToolListResult result = new ToolListResult();
        result.setTools(tools);
        return JsonRpcResponse.result(id, result);
    }

    private JsonRpcResponse<?> toolsCall(Object id, ToolCallParams params) {
        if (params == null || params.name == null || params.name.isBlank()) {
            return JsonRpcResponse.error(id, -32602, "Invalid params: name is required");
        }

        Map<String, Object> args = params.arguments != null ? params.arguments : Collections.emptyMap();

        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
        Optional<ToolCallback> cbOpt = Arrays.stream(callbacks)
                .filter(cb -> params.name.equalsIgnoreCase(getToolName(cb)))
                .findFirst();

        if (cbOpt.isEmpty()) {
            return JsonRpcResponse.error(id, -32601, "Tool not found: " + params.name);
        }

        Object raw;
        try {
            raw = invokeTool(cbOpt.get(), args);
        } catch (Exception e) {
            return JsonRpcResponse.error(id, -32000, "Tool execution failed: " + e.getMessage());
        }

        ToolCallResult result = new ToolCallResult();
        String text;
        try {
            if (raw == null) {
                text = "";
            } else if (raw instanceof String s) {
                text = s;
            } else if (raw instanceof Number || raw instanceof Boolean) {
                text = String.valueOf(raw);
            } else {
                text = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(raw);
            }
        } catch (Exception e) {
            text = String.valueOf(raw);
        }
        result.addText(text);
        return JsonRpcResponse.result(id, result);
    }


    private ToolInfo toToolInfo(ToolCallback cb) {
        ToolInfo info = new ToolInfo();
        String name = getToolName(cb);
        String description = getToolDescription(cb);
        JsonNode schema = getInputSchema(cb);
        info.setName(name);
        info.setDescription(description);
        info.setInputSchema(schema != null ? schema : objectMapper.createObjectNode());
        return info;
    }

    private String getToolName(ToolCallback cb) {
        try {
            var mSpec = cb.getClass().getMethod("getToolSpecification");
            Object spec = mSpec.invoke(cb);
            if (spec != null) {
                try {
                    var mName = spec.getClass().getMethod("getName");
                    Object v = mName.invoke(spec);
                    if (v != null) return String.valueOf(v);
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (NoSuchMethodException ignored) {
            // fallthrough
        } catch (Exception e) {
            // ignore and continue
        }
        try {
            var m = cb.getClass().getMethod("getName");
            Object v = m.invoke(cb);
            if (v != null) return String.valueOf(v);
        } catch (Exception ignored) {}
        return cb.getClass().getSimpleName();
    }

    private String getToolDescription(ToolCallback cb) {
        try {
            var mSpec = cb.getClass().getMethod("getToolSpecification");
            Object spec = mSpec.invoke(cb);
            if (spec != null) {
                try {
                    var mDesc = spec.getClass().getMethod("getDescription");
                    Object v = mDesc.invoke(spec);
                    if (v != null) return String.valueOf(v);
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
        }
        try {
            var m = cb.getClass().getMethod("getDescription");
            Object v = m.invoke(cb);
            if (v != null) return String.valueOf(v);
        } catch (Exception ignored) {}
        return "";
    }

    private JsonNode getInputSchema(ToolCallback cb) {
        try {
            var mSpec = cb.getClass().getMethod("getToolSpecification");
            Object spec = mSpec.invoke(cb);
            if (spec != null) {
                try {
                    var mSchema = spec.getClass().getMethod("getInputSchema");
                    Object v = mSchema.invoke(spec);
                    if (v instanceof JsonNode jn) return jn;
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
        }
        try {
            var m = cb.getClass().getMethod("getInputSchema");
            Object v = m.invoke(cb);
            if (v instanceof JsonNode jn) return jn;
        } catch (Exception ignored) {}
        return null;
    }

    private Object invokeTool(ToolCallback cb, Map<String, Object> arguments) throws Exception {
        Class<?> cbClass = cb.getClass();
        try {
            var m = cbClass.getMethod("call", Map.class);
            return m.invoke(cb, arguments);
        } catch (NoSuchMethodException ignored) {}
        try {
            var m = cbClass.getMethod("call", JsonNode.class);
            JsonNode node = objectMapper.valueToTree(arguments);
            return m.invoke(cb, node);
        } catch (NoSuchMethodException ignored) {}
        try {
            var m = cbClass.getMethod("call", String.class);
            String payload = objectMapper.writeValueAsString(arguments);
            return m.invoke(cb, payload);
        } catch (NoSuchMethodException ignored) {}
        if (arguments.isEmpty()) {
            try {
                var m = cbClass.getMethod("call");
                return m.invoke(cb);
            } catch (NoSuchMethodException ignored) {}
        }
        for (var m : cbClass.getMethods()) {
            if (m.getName().equals("call") && m.getParameterCount() == 1) {
                Class<?> p = m.getParameterTypes()[0];
                Object arg;
                if (p.isAssignableFrom(Map.class)) {
                    arg = arguments;
                } else if (JsonNode.class.isAssignableFrom(p)) {
                    arg = objectMapper.valueToTree(arguments);
                } else if (p == String.class) {
                    arg = objectMapper.writeValueAsString(arguments);
                } else {
                    arg = objectMapper.convertValue(arguments, p);
                }
                return m.invoke(cb, arg);
            }
        }
        throw new NoSuchMethodException("No compatible 'call' method found on ToolCallback: " + cbClass.getName());
    }

    // ---- DTOs for JSON-RPC ----

    public static class JsonRpcRequest {
        public String jsonrpc;
        public Object id;
        public String method;
        public Object params;
    }

    public static class JsonRpcResponse<T> {
        public String jsonrpc = "2.0";
        public Object id;
        public T result;
        public ErrorObj error;

        public static <T> JsonRpcResponse<T> result(Object id, T result) {
            JsonRpcResponse<T> resp = new JsonRpcResponse<>();
            resp.id = id;
            resp.result = result;
            return resp;
        }

        public static JsonRpcResponse<?> error(Object id, int code, String message) {
            JsonRpcResponse<?> resp = new JsonRpcResponse<>();
            resp.id = id;
            resp.error = new ErrorObj(code, message);
            return resp;
        }

        public static class ErrorObj {
            public int code;
            public String message;
            public ErrorObj(int code, String message) {
                this.code = code;
                this.message = message;
            }
        }
    }

    public static class ToolListResult {
        private List<ToolInfo> tools;
        public List<ToolInfo> getTools() { return tools; }
        public void setTools(List<ToolInfo> tools) { this.tools = tools; }
    }

    public static class ToolInfo {
        private String name;
        private String description;
        private JsonNode inputSchema;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public JsonNode getInputSchema() { return inputSchema; }
        public void setInputSchema(JsonNode inputSchema) { this.inputSchema = inputSchema; }
    }

    public static class ToolCallParams {
        public String name;
        public Map<String, Object> arguments;
    }

    public static class ToolCallResult {
        private List<Content> content = new ArrayList<>();
        public List<Content> getContent() { return content; }
        public void setContent(List<Content> content) { this.content = content; }
        public void addText(String text) { this.content.add(Content.text(text)); }
    }

    public static class Content {
        private String type;
        private String text;
        public String getType() { return type; }
        public String getText() { return text; }
        public static Content text(String t) {
            Content c = new Content();
            c.type = "text";
            c.text = t;
            return c;
        }
    }
}