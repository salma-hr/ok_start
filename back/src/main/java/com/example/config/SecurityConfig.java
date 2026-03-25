package com.example.config;

import com.example.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            // ── Swagger / OpenAPI ──────────────────────────
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/webjars/**"
    };
     @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**", "/webjars/**").permitAll()

                
                // Admin
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ✅ Profil self-update — TOUS les rôles connectés
                .requestMatchers("/api/profil/**").authenticated()

                // GET — tous les rôles connectés
                .requestMatchers(HttpMethod.GET,
                        "/api/processus/**", "/api/machines/**",
                        "/api/criteres/**", "/api/checklists/**",
                        "/api/segments/**", "/api/plants/**",
                        "/api/sites/**", "/api/dashboard/**",
                        "/api/notifications/**")
                .authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/notifications/**").authenticated()
                .requestMatchers("/api/ai/**").hasAnyRole("ADMIN", "PPO") // IA accessible uniquement aux PPO et Admin
                // Checklist — import PDF (Admin et PPO)
                .requestMatchers(HttpMethod.POST, "/api/checklists/import-pdf")
                .hasAnyRole("ADMIN", "PPO")

                // Checklist — soumettre (Opérateur uniquement)
                .requestMatchers(HttpMethod.POST, "/api/checklists/soumettre")
                .hasAnyRole("OPERATEUR", "ADMIN")

                // Checklist — validation N1 (Chef de ligne)
                .requestMatchers(HttpMethod.PATCH, "/api/checklists/*/valider-n1")
                .hasAnyRole("CHEF_LIGNE", "ADMIN")

                // Checklist — validation N2 (Technicien)
                .requestMatchers(HttpMethod.PATCH, "/api/checklists/*/valider-n2")
                .hasAnyRole("TECHNICIEN", "ADMIN")

                // Checklist — validation finale (Agent Qualité)
                .requestMatchers(HttpMethod.PATCH, "/api/checklists/*/valider-final")
                .hasAnyRole("AGENT_QUALITE", "ADMIN")

                // Checklist — rejet (Chef de ligne, Technicien, Agent Qualité)
                .requestMatchers(HttpMethod.PATCH, "/api/checklists/*/rejeter")
                .hasAnyRole("CHEF_LIGNE", "TECHNICIEN", "AGENT_QUALITE", "ADMIN")

                // IA PDF
                .requestMatchers(HttpMethod.POST, "/api/ai/**").hasAnyRole("PPO", "ADMIN")

                // CRUD processus / machines / critères
                .requestMatchers(HttpMethod.POST,
                        "/api/processus/**", "/api/machines/**", "/api/criteres/**")
                .hasAnyRole("PPO", "ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/processus/**", "/api/machines/**", "/api/criteres/**")
                .hasAnyRole("PPO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE,
                        "/api/processus/**", "/api/machines/**", "/api/criteres/**")
                .hasAnyRole("PPO", "ADMIN")

                .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(401);
                            response.getWriter().write("Non authentifié");
                        })
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(403);
                            response.getWriter().write("Accès refusé : rôle insuffisant");
                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}