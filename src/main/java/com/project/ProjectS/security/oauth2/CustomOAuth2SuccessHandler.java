package com.project.ProjectS.security.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.project.ProjectS.service.UserService;
import com.project.ProjectS.model.UserDTO;
import com.project.ProjectS.security.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomOAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {
	 @Autowired
	    private UserService userService;
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

	    String email = oauthUser.getAttribute("email");
	    String name = oauthUser.getAttribute("name");

	    UserDTO user =
	            userService.findOrCreateGoogleUser(name, email);

	    String token = jwtUtil.generateToken(
	            user.getEmail(),
	            user.getRoleName()
	    );

	    response.sendRedirect(
	            "http://localhost:5173/oauth2/success?token=" + token
	    );
	}
}