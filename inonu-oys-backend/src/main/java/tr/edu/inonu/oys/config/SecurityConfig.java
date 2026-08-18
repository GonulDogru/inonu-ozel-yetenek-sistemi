package tr.edu.inonu.oys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/departments/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/system-settings").authenticated()
                .requestMatchers("/api/system-settings/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/users/**", "/api/departments/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "DEPARTMENT_ADMIN")
                .requestMatchers("/api/audit-logs/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "DEPARTMENT_ADMIN")
                .requestMatchers("/api/applications/all", "/api/applications/*/status",
                                 "/api/applications/*/scores", "/api/applications/*/academic").hasAnyRole("ADMIN", "SUPER_ADMIN", "DEPARTMENT_ADMIN")
                .requestMatchers("/api/jury/assign", "/api/jury/remove").hasAnyRole("ADMIN", "SUPER_ADMIN", "DEPARTMENT_ADMIN")
                .requestMatchers("/api/candidate-jury-assignments/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "DEPARTMENT_ADMIN")
                .requestMatchers("/api/placements/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "DEPARTMENT_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/exam-sessions/my").hasRole("JURY")
                .requestMatchers("/api/exam-sessions/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "DEPARTMENT_ADMIN")
                .requestMatchers("/api/classrooms/**", "/api/jury-availability/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "DEPARTMENT_ADMIN")
                .requestMatchers("/api/jury/**").hasRole("JURY")
                .requestMatchers("/api/applications/apply", "/api/applications/by-username/**").hasRole("APPLICANT")
                .requestMatchers("/api/applications/*/exam-document",
                                 "/api/applications/*/result-document", "/api/files/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:517*",
                "http://127.0.0.1:517*"));
        // İzin verilen metotlara "DELETE" eklendi
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
