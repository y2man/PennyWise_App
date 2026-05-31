package com.pennywise.config;
import jakarta.servlet.FilterChain; import jakarta.servlet.ServletException; import jakarta.servlet.http.*;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.util.List;

@Configuration @EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    public SecurityConfig(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    /**
     * Provide a no-op UserDetailsService so Spring Boot does NOT auto-configure
     * an InMemoryUserDetailsManager that generates a random password and interferes
     * with our JWT-based authentication.
     */
    @Bean public UserDetailsService userDetailsService() {
        // We never use username/password form login — authentication is JWT-only.
        // Returning an empty manager prevents Spring Security's auto-configuration
        // from creating a conflicting bean with a random password.
        return new InMemoryUserDetailsManager();
    }

    @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (stateless JWT API — no cookies)
            .csrf(c -> c.disable())
            // CORS handled by corsSource bean
            .cors(c -> c.configurationSource(corsSource()))
            // Disable HTTP Basic and Form Login — we use JWT only
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable())
            // Stateless — no HTTP session, all auth via JWT
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Auth rules: /api/auth/** is public, everything else requires a valid JWT
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            // JWT filter runs before Spring's UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean public OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                    throws ServletException, IOException {
                String header = req.getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    String token = header.substring(7);
                    try {
                        if (jwtUtil.isValid(token)) {
                            String email = jwtUtil.extractEmail(token);
                            UserDetails ud = User.withUsername(email)
                                .password("").authorities("USER").build();
                            UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        } else {
                            System.err.println("[JWT] Invalid/expired token for: " + req.getRequestURI());
                            // Clear any stale context
                            SecurityContextHolder.clearContext();
                        }
                    } catch (Exception e) {
                        System.err.println("[JWT] Token error: " + e.getMessage() + " for " + req.getRequestURI());
                        SecurityContextHolder.clearContext();
                    }
                } else if (!req.getRequestURI().startsWith("/api/auth/")) {
                    System.err.println("[JWT] No Bearer token for: " + req.getRequestURI());
                }
                chain.doFilter(req, res);
            }
        };
    }

    @Bean public CorsConfigurationSource corsSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
