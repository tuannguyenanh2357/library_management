package com.library.controller;

import com.library.dto.request.AuthenticationRequest;
import com.library.dto.request.IntrospectRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.AuthenticationResponse;
import com.library.dto.response.IntrospectResponse;
import com.library.dto.response.MemberResponse;
import com.library.service.interfaces.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
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

        ResponseCookie springCookie = ResponseCookie.from("auth_token", authResponse.getToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(90 * 60)
                .sameSite("Strict") // Trình duyệt sẽ không gửi cookie này đi nếu request đến từ một domain khác
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, springCookie.toString());

        // Xóa token khỏi response body
        authResponse.setToken(null);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie springCookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // Xóa cookie
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, springCookie.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody RegisterRequest request) {
        MemberResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/introspect")
    public ResponseEntity<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest request)
            throws Exception {
        IntrospectResponse response = authenticationService.introspect(request);
        return ResponseEntity.ok(response);
    }

}
