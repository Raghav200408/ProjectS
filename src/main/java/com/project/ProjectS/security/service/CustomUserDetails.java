package com.project.ProjectS.security.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.project.ProjectS.model.UserDTO;

public class CustomUserDetails implements UserDetails {

    private final UserDTO user;

    public CustomUserDetails(UserDTO user) {
        this.user = user;
    }

    /**
     * Return complete user object
     */
    public UserDTO getUser() {
        return user;
    }

    /**
     * Spring Security Role
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        String role = switch (user.getRoleId()) {
            case 1 -> "ROLE_SUPER_ADMIN";
            case 2 -> "ROLE_BRANCH_ADMIN";
            default -> "ROLE_STUDENT";
        };

        System.out.println("Role ID = " + user.getRoleId());
        System.out.println("Authority = " + role);

        return List.of(new SimpleGrantedAuthority(role));
    }

    /**
     * Password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Username (Email)
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}