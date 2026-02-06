package org.example.docvanta_bcakend.config;

import org.example.docvanta_bcakend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsService userDetailsService,
            CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (exclude admin sub-path)
                        .requestMatchers("/api/auth/admin/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // ── Personnel management: write = SYSTEM_ADMINISTRATOR only, read = admin + receptionist ──
                        .requestMatchers(HttpMethod.POST, "/api/personnel/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/personnel/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/personnel/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/personnel/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "RECEPTIONIST")

                        // ── Clinic management: SYSTEM_ADMINISTRATOR only for write, authenticated for read ──
                        .requestMatchers(HttpMethod.POST, "/api/clinics/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/clinics/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/clinics/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/clinics/**").authenticated()

                        // ── Clinical Department management: SYSTEM_ADMINISTRATOR only for write ──
                        .requestMatchers(HttpMethod.POST, "/api/clinical-departments/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/clinical-departments/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/clinical-departments/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/clinical-departments/**").authenticated()

                        // ── Practitioner: SYSTEM_ADMINISTRATOR can write, authenticated can read ──
                        .requestMatchers(HttpMethod.DELETE, "/api/practitioners/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/practitioners/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER")
                        .requestMatchers(HttpMethod.GET, "/api/practitioners/**").authenticated()

                        // ── Patient: SYSTEM_ADMINISTRATOR can delete, SYSTEM_ADMINISTRATOR+PATIENT can update ──
                        .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/patients/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/patients/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER", "RECEPTIONIST", "PATIENT")

                        // ── Appointments: admin + practitioner + receptionist + patient can interact ──
                        .requestMatchers(HttpMethod.POST, "/api/appointments/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER", "RECEPTIONIST", "PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/appointments/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.PATCH, "/api/appointments/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER", "RECEPTIONIST", "PATIENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/appointments/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "RECEPTIONIST")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/**").authenticated()

                        // ── Medical Documents: SYSTEM_ADMINISTRATOR+PRACTITIONER can create/update, SYSTEM_ADMINISTRATOR can delete ──
                        .requestMatchers(HttpMethod.POST, "/api/medical-documents/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER")
                        .requestMatchers(HttpMethod.PUT, "/api/medical-documents/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER")
                        .requestMatchers(HttpMethod.PATCH, "/api/medical-documents/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER")
                        .requestMatchers(HttpMethod.DELETE, "/api/medical-documents/**").hasAnyRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/medical-documents/**").authenticated()

                        // ── Patient Records: SYSTEM_ADMINISTRATOR+PRACTITIONER can manage ──
                        .requestMatchers(HttpMethod.POST, "/api/patient-records/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER")
                        .requestMatchers(HttpMethod.PUT, "/api/patient-records/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "PRACTITIONER")
                        .requestMatchers(HttpMethod.DELETE, "/api/patient-records/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/patient-records/**").authenticated()

                        // ── Medical Acts: authenticated can read, only SYSTEM_ADMINISTRATOR can write ──
                        .requestMatchers(HttpMethod.GET, "/api/medical-acts/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/medical-acts/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/medical-acts/**").hasRole("SYSTEM_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/medical-acts/**").hasRole("SYSTEM_ADMINISTRATOR")

                        // ── Billing: SYSTEM_ADMINISTRATOR+RECEPTIONIST ──
                        .requestMatchers("/api/invoices/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "RECEPTIONIST")
                        .requestMatchers("/api/payments/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "RECEPTIONIST")

                        // ── Admin User Management: SYSTEM_ADMINISTRATOR only ──
                        .requestMatchers("/api/admin/**").hasRole("SYSTEM_ADMINISTRATOR")

                        // Everything else requires authentication
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
