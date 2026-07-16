package com.project.ProjectS.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.ProjectS.model.UserDTO;
import com.project.ProjectS.service.UserService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        UserDTO user = userService.findByEmail(email);

        if (user == null) {

            throw new UsernameNotFoundException(
                    "User not found with email: " + email);

        }

        return new CustomUserDetails(user);

    }

}