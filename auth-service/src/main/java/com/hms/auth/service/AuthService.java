package com.hms.auth.service;

import com.hms.auth.dto.AuthResponse;
import com.hms.auth.dto.LoginRequest;
import com.hms.auth.dto.ProfileResponse;
import com.hms.auth.dto.RegisterRequest;

public interface AuthService {
    void registerUser(RegisterRequest request);
    AuthResponse loginUser(LoginRequest request);
    ProfileResponse getProfile(String email);
    void changePassword(String email, String newPassword);
}
