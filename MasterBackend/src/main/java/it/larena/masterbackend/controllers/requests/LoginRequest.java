package it.larena.masterbackend.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String password;
}
