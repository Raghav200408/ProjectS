package com.project.ProjectS.config;

import com.project.ProjectS.security.filter.JwtAuthenticationFilter;
import com.project.ProjectS.security.oauth2.CustomOAuth2SuccessHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
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
								"/login/**",
								"/error",
								"/api/college/**",
								"/api/branch/**",
								"/api/course/**",
								"/api/section/**",
								"/api/exam/**",
								"/api/exam-question/**",
								"/api/table-names/**",
								"/api/table-headers/**",
								"/api/table-attributes/**",
								"/api/question-categories/**",
								"/api/roles/**",
								"/api/chapter/**",
								"/api/rule-engines/**",
								"/api/dashboard/**",
								"/api/questions/**",
								"/api/users/superAdmin",
								"/api/users/superAdmins",
								"/api/users/branchAdmins",
								"/api/users/students",
								"/api/users/superAdmin/**",
								"/api/users/excel/upload",
								"/api/users/students_Guest/**",
								"/api/users/all/**"




						).permitAll()
								// =========================
// POST APIs
// =========================

								.requestMatchers(HttpMethod.POST,
										"/api/users/superAdmin",
										"/api/users/branchAdmin")
								.hasRole("SUPER_ADMIN")

								.requestMatchers(HttpMethod.POST,
										"/api/users/student")
								.hasAnyRole("SUPER_ADMIN", "BRANCH_ADMIN")

								.requestMatchers(HttpMethod.POST,
										"/api/users/guest/register")
								.permitAll()


// =========================
// GET APIs
// =========================

								.requestMatchers(HttpMethod.GET,
										"/api/users/superAdmins",
										"/api/users/superAdmin/**")
								.hasRole("SUPER_ADMIN")

								.requestMatchers(HttpMethod.GET,
										"/api/users/branchAdmins",
										"/api/users/branchAdmin/**")
								.hasRole("SUPER_ADMIN")

								.requestMatchers(HttpMethod.GET,
										"/api/users/students",
										"/api/users/student/**")
								.hasAnyRole("SUPER_ADMIN", "BRANCH_ADMIN")

								.requestMatchers(HttpMethod.GET,
										"/api/users/guests",
										"/api/users/guest/**")
								.hasRole("SUPER_ADMIN")


// =========================
// PUT APIs
// =========================

								.requestMatchers(HttpMethod.PUT,
										"/api/users/superAdmin/**")
								.hasRole("SUPER_ADMIN")

								.requestMatchers(HttpMethod.PUT,
										"/api/users/branchAdmin/**")
								.hasRole("SUPER_ADMIN")

								.requestMatchers(HttpMethod.PUT,
										"/api/users/student/**")
								.hasAnyRole("SUPER_ADMIN", "BRANCH_ADMIN")

								.requestMatchers(HttpMethod.PUT,
										"/api/users/guest/**")
								.hasRole("SUPER_ADMIN")


// =========================
// DELETE APIs
// =========================

								.requestMatchers(HttpMethod.DELETE,
										"/api/users/superAdmin/**")
								.hasRole("SUPER_ADMIN")

								.requestMatchers(HttpMethod.DELETE,
										"/api/users/branchAdmin/**")
								.hasRole("SUPER_ADMIN")

								.requestMatchers(HttpMethod.DELETE,
										"/api/users/student/**")
								.hasAnyRole("SUPER_ADMIN", "BRANCH_ADMIN")

								.requestMatchers(HttpMethod.DELETE,
										"/api/users/guest/**")
								.hasRole("SUPER_ADMIN")






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