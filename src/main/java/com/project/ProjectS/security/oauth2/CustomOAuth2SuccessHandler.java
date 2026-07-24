package com.project.ProjectS.security.oauth2;

import com.project.ProjectS.entity.Role;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.repository.RoleRepository;
import com.project.ProjectS.repository.UserRepository;
import com.project.ProjectS.security.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler
		extends SimpleUrlAuthenticationSuccessHandler {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication)
			throws IOException {

		OAuth2User oauthUser =
				(OAuth2User) authentication.getPrincipal();

		String googleId = oauthUser.getAttribute("sub");
		String email = oauthUser.getAttribute("email");
		String name = oauthUser.getAttribute("name");
		String picture = oauthUser.getAttribute("picture");

		if (email == null || email.isBlank()) {
			throw new RuntimeException(
					"Email not received from Google"
			);
		}

		User user = userRepository.findByEmail(email)
				.orElse(null);

		// New Google user -> create as GUEST
		if (user == null) {

			Role guestRole = roleRepository
					.findByRoleName("GUEST")
					.orElseThrow(() ->
							new RuntimeException(
									"GUEST role not found"
							)
					);

			user = new User();

			user.setName(name);
			user.setEmail(email);

			user.setGoogleId(googleId);
			user.setProfilePicture(picture);

			user.setLoginType("GOOGLE");

			user.setRole(guestRole);

			user = userRepository.save(user);

		} else {

			// Existing account logging in with Google
			if (user.getGoogleId() == null) {
				user.setGoogleId(googleId);
			}

			if (picture != null) {
				user.setProfilePicture(picture);
			}

			userRepository.save(user);
		}

		String roleName =
				user.getRole().getRoleName();

		String token =
				jwtUtil.generateToken(
						user.getEmail(),
						"ROLE_" + roleName
				);

		response.sendRedirect(
				"http://localhost:5173/oauth2/success?token="
						+ token
		);
	}
}