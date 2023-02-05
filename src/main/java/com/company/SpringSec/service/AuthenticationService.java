package com.company.SpringSec.service;

import com.company.SpringSec.dto.UserLoginResponse;
import com.company.SpringSec.dto.request.SignInRequest;
import com.company.SpringSec.security.UserPrincipal;
import com.company.SpringSec.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Value("${authentication.jwt.access.expiration-in-ms}")
    private Long JWT_ACCESS_EXPIRATION_IN_MS;
    @Value("${authentication.jwt.refresh.expiration-in-ms}")
    private Long JWT_REFRESH_EXPIRATION_IN_MS;

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }




    // private util methods

}
