package com.indigo.synapse.iam.interfaces.controller;

import com.indigo.synapse.iam.application.command.LoginCommand;
import com.indigo.synapse.iam.application.service.IamAuthApplicationService;
import com.indigo.synapse.iam.application.command.CreateUserCommand;
import com.indigo.synapse.iam.interfaces.request.LoginRequest;
import com.indigo.synapse.iam.interfaces.request.CreateUserRequest;
import com.indigo.synapse.iam.interfaces.response.LoginResponse;
import com.indigo.synapse.iam.interfaces.response.UserResponse;
import com.indigo.synapse.web.response.ApiResponse;
import com.indigo.synapse.web.trace.TraceContext;
import com.indigo.synapse.web.trace.TraceIdGenerator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final IamAuthApplicationService authApplicationService;

    public AuthController(IamAuthApplicationService authApplicationService) {
        if (authApplicationService == null) {
            throw new IllegalArgumentException("authApplicationService must not be null");
        }
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(
                request.getClientId(),
                request.getUsername(),
                request.getPassword(),
                TraceContext.currentTraceId().orElseGet(TraceIdGenerator::generate)
        );
        return ApiResponse.success(LoginResponse.from(authApplicationService.login(command)));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.getUsername(),
                request.getDisplayName(),
                request.getPassword(),
                request.getRoleCodes(),
                TraceContext.currentTraceId().orElseGet(TraceIdGenerator::generate)
        );
        return ApiResponse.success(UserResponse.from(authApplicationService.createUser(command)));
    }
}
