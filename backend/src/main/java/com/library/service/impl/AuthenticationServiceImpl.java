package com.library.service.impl;

import com.library.dto.request.AuthenticationRequest;
import com.library.dto.request.IntrospectRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.AuthenticationResponse;
import com.library.dto.response.IntrospectResponse;
import com.library.dto.response.MemberResponse;
import com.library.entity.Member;
import com.library.entity.enums.MemberRole;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.mapper.MemberMapper;
import com.library.repository.MemberRepository;
import com.library.service.interfaces.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    MemberRepository memberRepository;
    MemberMapper memberMapper;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String signerKey;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        String rawPassword = request.getPassword();

        if (username == null || username.isBlank() || rawPassword == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = generateToken(member);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .username(member.getUsername())
                .role(member.getRole() != null ? member.getRole().name() : "MEMBER")
                .memberId(member.getId())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberResponse register(RegisterRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        String phoneStr = request.getPhone() != null ? request.getPhone().trim() : "";
        if (!phoneStr.isBlank() && memberRepository.existsByPhone(phoneStr)) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .name(request.getName())
                .phone(phoneStr)
                .address(request.getAddress())
                .role(MemberRole.MEMBER)
                .isActive(true)
                .build();

        member = memberRepository.save(member);
        return memberMapper.toMemberResponse(member);
    }

    // hàm tạo JWT Token sau khi người dùng đăng nhập thành công
    private String generateToken(Member member) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        String userRole = member.getRole() != null ? member.getRole().name() : "MEMBER";

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(member.getUsername())
                .issuer("library-management")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(90, ChronoUnit.MINUTES).toEpochMilli()))
                .claim("scope", userRole)
                .claim("memberId", member.getId())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Không thể tạo token", e);
            throw new RuntimeException(e);
        }
    }

    // kiểm tra toke có đúng không - token còn hạn không
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();

        try {
            JWSVerifier verifier = new MACVerifier(signerKey.getBytes());

            SignedJWT signedJWT = SignedJWT.parse(token);

            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            var verified = signedJWT.verify(verifier);

            return IntrospectResponse.builder()
                    .valid(verified && expiryTime.after(new Date()))
                    .build();
        } catch (ParseException | JOSEException e) {
            log.warn("Token không hợp lệ khi introspect: {}", e.getMessage());
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }

}
