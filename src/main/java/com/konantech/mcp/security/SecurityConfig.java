package
com.konantech.mcp.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Configuration
public class SecurityConfig {

    @Value("${auth.server.introspect-url}")
    private String introspectUrl;

    @Value("${auth.server.client-id}")
    private String clientId;

    @Value("${auth.server.client-secret}")
    private String clientSecret;

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(introspectUrl, clientId, clientSecret),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    static class JwtFilter implements Filter {
        private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
        private final String introspectUrl;
        private final String basicAuth;
        private final HttpClient httpClient;
        private final ObjectMapper objectMapper;

        public JwtFilter(String introspectUrl, String clientId, String clientSecret) {
            this.introspectUrl = introspectUrl;
            this.basicAuth = "Basic " + Base64.getEncoder()
                    .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
            this.httpClient = HttpClient.newHttpClient();
            this.objectMapper = new ObjectMapper();
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            String authHeader = req.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token required");
                return;
            }

            String token = authHeader.substring(7);

            try {
                boolean isValid = introspectToken(token);

                if (isValid) {
                    log.info("Token validation successful");
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList())
                    );
                    chain.doFilter(request, response);
                } else {
                    log.warn("Token validation failed: inactive token");
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                }
            } catch (Exception e) {
                log.error("Token introspection error", e);
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token validation failed");
            }
        }

        private boolean introspectToken(String token) throws IOException, InterruptedException {
            String body = "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(introspectUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", basicAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            log.debug("Sending introspection request to: {}", introspectUrl);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            log.debug("Introspection response status: {}", response.statusCode());
            log.debug("Introspection response body: {}", response.body());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                return jsonNode.has("active") && jsonNode.get("active").asBoolean();
            }

            return false;
        }
    }
}