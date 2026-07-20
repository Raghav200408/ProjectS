package com.project.ProjectS.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.project.ProjectS.security.filter.JwtAuthenticationFilter;
import com.project.ProjectS.security.oauth2.CustomOAuth2SuccessHandler;
import com.project.ProjectS.security.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth

						.requestMatchers(
								"/",
								"/error",
								"/api/users/register",
								"/api/users/login",
								"/oauth2/**",
								"/login/**",

								"/api/college/**",
								"/api/branch/**",
								"/api/course/**",
								"/api/section/**",
								"/api/exam/**",
								"/api/exam-question/**"
								"/api/table-names/**",
								"/api/table-headers/**",
								"/api/table-attributes/**",
								"/api/question-categories/**",
								"/api/roles/**"

						).permitAll()
                	    .requestMatchers(
                	            "/api/users/me"
                	    ).authenticated()

                	    .requestMatchers(
                	            HttpMethod.GET,
                	            "/api/users"
                	    ).hasRole("SUPER_ADMIN")

                	    .requestMatchers(
                	            HttpMethod.GET,
                	            "/api/users/*"
                	    ).hasRole("SUPER_ADMIN")

                	    .requestMatchers(
                	            HttpMethod.PUT,
                	            "/api/users/*"
                	    ).hasRole("SUPER_ADMIN")

                	    .requestMatchers(
                	            HttpMethod.DELETE,
                	            "/api/users/*"
                	    ).hasRole("SUPER_ADMIN")

                	   
                	    .anyRequest()
                	    .authenticated()
                	)

                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customOAuth2SuccessHandler)
                );

        return http.build();

    }

}