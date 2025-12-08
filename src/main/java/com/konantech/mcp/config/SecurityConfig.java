package com.konantech.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // custom jwt auth converter
    private class NhisJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<String> permissions = jwt.getClaimAsStringList("authorities");
            if (permissions == null) { permissions = List.of(); }
            return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        }
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter( new NhisJwtGrantedAuthoritiesConverter() );

        http.csrf(AbstractHttpConfigurer::disable) // 전부 다 허용
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        // oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter) // jwk 방식
                        oauth2.opaqueToken(Customizer.withDefaults()) // opaque.introspection 방식
                );
        return http.build();
    }

    @Bean
    public OpaqueTokenIntrospector introspector() {
        // token 확인용
//        OpaqueTokenIntrospector delegate = new NimbusOpaqueTokenIntrospector(
//                "http://localhost:8080/portal/kauth/oauth2/introspect", // introspection URL
//                "admin-client",    // Resource server용 clientId
//                "q1vGRFSg7YULuKPTelTb61O7bMJW5H" // Resource server용 clientSecret
//        );
//        return new LoggingOpaqueTokenIntrospector(delegate);
        return new NimbusOpaqueTokenIntrospector(
                "http://localhost:8080/portal/kauth/oauth2/introspect", // introspection URL
                "admin-client",    // Resource server용 clientId
                "q1vGRFSg7YULuKPTelTb61O7bMJW5H"); // Resource server용 clientSecret
    }
/*
// token 확인용
@Slf4j
class LoggingOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final OpaqueTokenIntrospector delegate;

    public LoggingOpaqueTokenIntrospector(OpaqueTokenIntrospector delegate) {
        this.delegate = delegate;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        log.info("[introspec] introspection 요청 토큰");

        OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);

        log.info("[introspec] introspection 결과 attributes: {}", principal.getAttributes());
        log.info("[introspec] introspection 결과 authorities: {}", principal.getAuthorities());

        return principal;
    }
 */
}
