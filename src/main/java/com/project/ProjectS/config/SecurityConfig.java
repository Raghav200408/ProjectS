package com.project.ProjectS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            	    .requestMatchers(
            	        "/",
            	        "/login**",
            	        "/error",
            	        "/user",
            	        "/api/users/google-register"
            	    ).permitAll()

            	    .anyRequest().authenticated()
            	)
            .oauth2Login(oauth -> oauth
                    .defaultSuccessUrl("/user", true));
       
        return http.build();
    }

	
}