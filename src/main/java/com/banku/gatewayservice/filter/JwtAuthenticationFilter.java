package com.banku.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import com.banku.gatewayservice.config.SecurityConfig.SecurityProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final SecretKey key;
    private final SecurityProperties securityProperties;

    public JwtAuthenticationFilter(
            @Value("${jwt.secret}") String jwtSecret,
            SecurityProperties securityProperties) {
        super(Config.class);
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.securityProperties = securityProperties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            log.debug("Processing request for path: {}", path);

            // Skip JWT validation for public endpoints
            if (isPublicEndpoint(path)) {
                log.debug("Skipping JWT validation for public endpoint: {}", path);
                return chain.filter(exchange);
            }

            // Check if Authorization header exists
            if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                log.debug("No Authorization header found for path: {}", path);
                return handleError(exchange, "No Authorization header found", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("Invalid Authorization header format for path: {}", path);
                return handleError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            log.debug("Validating JWT token for path: {}", path);

            try {
                // Validate JWT token using the new API
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Add user ID to request headers for downstream services
                String userId = claims.getSubject();
                log.debug("JWT validation successful for user: {} at path: {}", userId, path);
                
                exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange);
            } catch (Exception e) {
                log.error("JWT validation failed for path: {} - Error: {}", path, e.getMessage());
                return handleError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicEndpoint(String path) {
        return securityProperties.getWhitelist().stream()
                .anyMatch(pattern -> path.matches(pattern.replace("**", ".*")));
    }

    private Mono<Void> handleError(org.springframework.web.server.ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(
                        String.format("{\"error\": \"%s\"}", message).getBytes()
                ))
        );
    }

    public static class Config {
        // Empty config class
    }
} 