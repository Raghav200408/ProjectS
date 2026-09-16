package com.project.ProjectS.config;

import com.project.ProjectS.security.filter.JwtAuthenticationFilter;
import com.project.ProjectS.security.oauth2.CustomOAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Authorization rules from Role_Based_Functionality_Readable.xlsx.
 */
@Configuration
public class SecurityConfig {
    private static final String SA = "SUPER_ADMIN";
    private static final String CA = "COLLEGE_ADMIN";
    private static final String BA = "BRANCH_ADMIN";
    private static final String ST = "STUDENT";
    private static final String GU = "GUEST";
    private static final String[] ALL = {SA, CA, BA, ST, GU};
    private static final String[] ADMINS = {SA, CA, BA};

    private final JwtAuthenticationFilter jwtFilter;
    private final CustomOAuth2SuccessHandler oauthSuccessHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          CustomOAuth2SuccessHandler oauthSuccessHandler) {
        this.jwtFilter = jwtFilter;
        this.oauthSuccessHandler = oauthSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public entry points only.
                        .requestMatchers("/api/auth/login", "/api/users/guest/register",
                                "/oauth2/**", "/login/**", "/error").permitAll()

                        // Dashboard and settings.
                        .requestMatchers("/api/dashboard/**", "/api/settings/**").hasAnyRole(ALL)

                        // Subscription overview/history is administrative; plans are available to all roles.
                        .requestMatchers(HttpMethod.GET, "/api/subscriptions/users/**",
                                "/api/subscriptions/history").hasAnyRole(ADMINS)
                        .requestMatchers("/api/subscriptions/plans/**",
                                "/api/subscriptions/courses/*/plans").hasAnyRole(ALL)
                        .requestMatchers(HttpMethod.POST, "/api/subscriptions/activate").hasAnyRole(ST, GU)
                        .requestMatchers(HttpMethod.GET, "/api/subscriptions/mine").hasAnyRole(ALL)
                        .requestMatchers(HttpMethod.POST, "/api/subscriptions/bulk-assign",
                                "/api/subscriptions/bulk", "/api/subscriptions/*/deactivate").hasAnyRole(ADMINS)

                        // Colleges, branches and sections.
                        .requestMatchers("/api/college/**", "/api/branch/**").hasAnyRole(SA, CA)
                        .requestMatchers("/api/section/**").hasAnyRole(ADMINS)

                        // Courses, subjects, chapters and topics: student/guest read-only.
                        .requestMatchers(HttpMethod.GET, "/api/course/**", "/api/subjects/**",
                                "/api/chapter/**", "/api/topics/**").hasAnyRole(ALL)
                        .requestMatchers("/api/course/**", "/api/subjects/**",
                                "/api/chapter/**", "/api/topics/**").hasAnyRole(ADMINS)

                        // Practice is available to all roles; authoring is Super Admin only.
                        .requestMatchers(HttpMethod.POST, "/api/mcq-questions/submit",
                                "/api/question_answers/**").hasAnyRole(ALL)
                        .requestMatchers(HttpMethod.GET, "/api/questions/**",
                                "/api/mcq-questions/**").hasAnyRole(ALL)
                        .requestMatchers(HttpMethod.POST, "/api/questions/**",
                                "/api/mcq-questions/**").hasRole(SA)
                        .requestMatchers(HttpMethod.PUT, "/api/questions/**",
                                "/api/mcq-questions/**").hasRole(SA)
                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**",
                                "/api/mcq-questions/**").hasRole(SA)
                        .requestMatchers("/api/question-types/**").hasRole(SA)
                        .requestMatchers(HttpMethod.GET, "/api/question_answers/**")
                        .hasAnyRole(SA, CA, BA, ST)
                        .requestMatchers(HttpMethod.PUT, "/api/question_answers/**")
                        .hasAnyRole(SA, CA, BA, ST)

                        // Exam/mock-exam attempts are allowed to all except Guest; management is Super Admin only.
                        .requestMatchers(HttpMethod.POST, "/api/exams/*/submit",
                                "/api/mock-exams/*/submit").hasAnyRole(SA, CA, BA, ST)
                        .requestMatchers(HttpMethod.GET, "/api/exams/*/questions",
                                "/api/mock-exams/*/questions").hasAnyRole(SA, CA, BA, ST)
                        .requestMatchers("/api/exams/**", "/api/mock-exams/**").hasRole(SA)
                        .requestMatchers("/api/mock-tests/**").hasAnyRole(SA, CA, BA, ST)

                        // Performance endpoints already derive organizational scope from the authenticated user.
                        .requestMatchers("/api/performance/super-admin/**").hasRole(SA)
                        .requestMatchers("/api/performance/college/**").hasRole(CA)
                        .requestMatchers("/api/performance/branch/**").hasRole(BA)
                        .requestMatchers("/api/performance/student/**").hasRole(ST)

                        // User administration.
                        .requestMatchers("/api/users/superAdmin/**", "/api/users/superAdmins").hasRole(SA)
                        .requestMatchers("/api/users/collegeAdmin/**").hasRole(SA)
                        .requestMatchers(
                                "/api/users/branchAdmin",
                                "/api/users/branchAdmin/**",
                                "/api/users/branchAdmins",
                                "/api/users/branchAdmins/**"
                        ).hasAnyRole(SA, CA)
                        .requestMatchers("/api/users/student/**", "/api/users/students")
                        .hasAnyRole(SA, CA, BA)
                        .requestMatchers("/api/users/guest/**", "/api/users/students_Guest/**",
                                "/api/users/all/**", "/api/users/excel/upload").hasRole(SA)

                        // Attendance: students can only use the self-service endpoint.
                        .requestMatchers(HttpMethod.GET, "/api/attendance/mine").hasRole(ST)
                        .requestMatchers(HttpMethod.GET, "/api/attendance/**").hasAnyRole(ADMINS)
                        .requestMatchers("/api/attendance/**").hasAnyRole(ADMINS)

                        // Table metadata and rules.
                        .requestMatchers("/api/table-names/**", "/api/table-headers/**",
                                "/api/table-attributes/**", "/api/roles/**").hasRole(SA)
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/rule-engines/**"
                        ).hasAnyRole(ALL)
                        // Results and certificates are not available to guests.
                        .requestMatchers("/api/answer_events/**", "/api/certificates/**")
                        .hasAnyRole(SA, CA, BA, ST)
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth.successHandler(oauthSuccessHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
