package com.monolith.users.api;

import com.monolith.users.api.dto.CreateUserRequest;
import com.monolith.users.api.dto.UserResponse;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(UUID id);
    boolean existsById(UUID id);
}
