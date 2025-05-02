package com.bookworm.presentation.controller;

import com.bookworm.infrastructure.jwt.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginController(JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }


//    @PostMapping("/login")
//    public ResponseEntity<?> getToken(@RequestBody
//                                      AccountCredentials credentials) {
//
//    }


}
