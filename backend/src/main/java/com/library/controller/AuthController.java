package com.library.controller;

import com.library.dto.request.AuthenticationRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.AuthenticationResponse;
import com.library.dto.response.MemberResponse;
import com.library.dto.response.UserResponse;
import com.library.service.interfaces.AuthenticationService;
import com.library.service.interfaces.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthenticationService authenticationService;
    MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request,
            HttpServletResponse response) {
        AuthenticationResponse authResponse = authenticationService.authenticate(request);

        ResponseCookie springCookie = ResponseCookie.from("auth_token", authResponse.getToken())
                .httpOnly(true) // chặn javascript đọc được token
                .secure(false)
                .path("/")
                .maxAge(45 * 60)
                .sameSite("Strict") // Trình duyệt sẽ không gửi cookie này đi nếu request đến từ một domain khác
                .build();
        // Gắn cái Cookie đó vào HTTP Header để gửi về trình duyệt
        response.addHeader(HttpHeaders.SET_COOKIE, springCookie.toString());

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









    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        MemberResponse member = memberService.getMemberByUsername(username);

        UserResponse userResponse = UserResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole() != null ? member.getRole().name() : "MEMBER")
                .build();

        return ResponseEntity.ok(userResponse);
    }
}
