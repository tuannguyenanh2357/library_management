package com.library.service.interfaces;

import com.library.dto.request.AuthenticationRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.request.IntrospectRequest;
import com.library.dto.response.AuthenticationResponse;
import com.library.dto.response.IntrospectResponse;
import com.library.dto.response.MemberResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
    MemberResponse register(RegisterRequest request);
    IntrospectResponse introspect(IntrospectRequest request) throws Exception;
}
