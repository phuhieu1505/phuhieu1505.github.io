package com.myproject.busticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.myproject.busticket.components.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider // ignore Bean warning we will get to that
    ) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/accounts/**").authenticated()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/booking/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/vnpay/**").permitAll()
                        .requestMatchers("/vnpay-return").permitAll()
                        .requestMatchers("/schedules/**").permitAll()
                        .requestMatchers("/roles/**").permitAll()
                        .requestMatchers("/permissions/**").permitAll()
                        .requestMatchers("/users/**").permitAll()
                        .requestMatchers("/home/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/theme-assets/**").permitAll()
                        .requestMatchers("/easy-bus/**").permitAll()
                        .requestMatchers("/easy-bus/dashboard").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/easy-bus/trip-management/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/easy-bus/route-management/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/easy-bus/checkpoint-management/**").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers("/easy-bus/bus-management/**").hasAuthority("ADMIN")
                        .requestMatchers("/easy-bus/booking-management/**").hasAuthority("ADMIN")
                        .requestMatchers("/easy-bus/bill-management/**").hasAuthority("ADMIN")
                        .requestMatchers("/easy-bus/account-management/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080")); // TODO: update backend url
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
