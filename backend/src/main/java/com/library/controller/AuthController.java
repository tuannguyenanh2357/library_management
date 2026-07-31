package com.library.controller;

import com.library.dto.request.AuthenticationRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.AuthenticationResponse;
import com.library.dto.response.MemberResponse;
import com.library.service.interfaces.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request,
            HttpServletResponse response) {
        AuthenticationResponse authResponse = authenticationService.authenticate(request);

        Cookie cookie = new Cookie("auth_token", authResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // đặt thành true nếu sử dụng HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(90 * 60); // 90 phút

        response.addCookie(cookie);

        // Xóa token khỏi response body
        authResponse.setToken(null);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Xóa cookie

        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody RegisterRequest request) {
        MemberResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/introspect")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> introspect(@Valid @RequestBody RegisterRequest request) {
        MemberResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }
}
