package com.project.ProjectS.model;

public class LoginResponseDTO {

    private boolean registered;

    private String token;         

    private UserDTO user;

    private GoogleUserDTO googleUser;

    public LoginResponseDTO(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }

	public LoginResponseDTO() {
		
	}

	public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public GoogleUserDTO getGoogleUser() {
        return googleUser;
    }

    public void setGoogleUser(GoogleUserDTO googleUser) {
        this.googleUser = googleUser;
    }

}