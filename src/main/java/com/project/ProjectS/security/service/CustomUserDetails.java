package com.project.ProjectS.security.service;

import com.project.ProjectS.entity.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Get complete User entity
    public User getUser() {
        return user;
    }

    // Get user's role/authority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        String roleName = user.getRole().getRoleName();

        String authority = "ROLE_" + roleName;

        return List.of(
                new SimpleGrantedAuthority(authority)
        );
    }

    // Encoded password from database
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // We use email as username
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