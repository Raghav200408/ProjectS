package com.project.ProjectS.model;

public class LoginResponseDTO {

    private boolean registered;

    private UserDTO user;

    private GoogleUserDTO googleUser;

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
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