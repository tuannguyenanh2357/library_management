package com.library.service.impl;

import com.library.dto.request.AuthenticationRequest;
import com.library.dto.request.IntrospectRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.AuthenticationResponse;
import com.library.dto.response.IntrospectResponse;
import com.library.dto.response.MemberResponse;
import com.library.entity.Member;
import com.library.entity.enums.MemberRole;
import com.library.mapper.MemberMapper;
import com.library.repository.MemberRepository;
import com.library.service.interfaces.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Data
public class AuthenticationServiceImpl implements AuthenticationService {
    MemberRepository memberRepository;
    MemberMapper memberMapper;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String signerKey;

    public IntrospectResponse introspect(IntrospectRequest request) throws Exception{
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        String rawPassword = request.getPassword();

        if (username == null || username.isBlank() || rawPassword == null) {
            throw new RuntimeException("Username and password are required");
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username or password incorrect"));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new RuntimeException("Username or password incorrect");
        }

        String token = generateToken(member);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    @Override
    public MemberResponse register(RegisterRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        String phoneStr = request.getPhone() != null ? request.getPhone().trim() : "";
        if (!phoneStr.isBlank() && memberRepository.existsByPhone(phoneStr)) {
            throw new RuntimeException("Phone number already exists");
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

    private String generateToken(Member member) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        String userRole = member.getRole() != null ? member.getRole().name() : "MEMBER";

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(member.getUsername())
                .issuer("library-management")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(90, ChronoUnit.MINUTES).toEpochMilli()
                ))
                .claim("scope", userRole)
                .claim("memberId", member.getId())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }
}
