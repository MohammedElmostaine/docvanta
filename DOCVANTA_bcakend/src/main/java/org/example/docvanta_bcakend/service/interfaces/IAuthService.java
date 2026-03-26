package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.AuthResponse;
import org.example.docvanta_bcakend.dto.LoginRequest;
import org.example.docvanta_bcakend.dto.RegisterRequest;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse adminCreateUser(RegisterRequest request);
}
