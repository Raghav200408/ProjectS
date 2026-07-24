package com.project.ProjectS.config;

import com.project.ProjectS.security.filter.JwtAuthenticationFilter;
import com.project.ProjectS.security.oauth2.CustomOAuth2SuccessHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http

				.csrf(csrf -> csrf.disable())

				.cors(cors -> {})

				.sessionManagement(session ->
						session.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS
						)
				)

				.authorizeHttpRequests(auth -> auth



						.requestMatchers(
								"/api/auth/login",
								"/api/users/guest/register",
								"/oauth2/**",
								"/login/**"
						).permitAll()




						.requestMatchers(
								"/api/users/superAdmin",
								"/api/users/branchAdmin"
						).hasRole("SUPER_ADMIN")




						.requestMatchers(
								"/api/users/student"
						).hasAnyRole(
								"SUPER_ADMIN",
								"BRANCH_ADMIN"
						)




						.anyRequest()
						.authenticated()
				)




				.oauth2Login(oauth ->
						oauth.successHandler(
								customOAuth2SuccessHandler
						)
				)


				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class
				);

		return http.build();
	}
}