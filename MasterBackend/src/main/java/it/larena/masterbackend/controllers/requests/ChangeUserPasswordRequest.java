package it.larena.masterbackend.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ChangeUserPasswordRequest {

    @NotNull
    @NotBlank
    private String previousPassword;

    @NotNull
    @NotBlank
    private String password;


    @NotNull
    @NotBlank
    private String passwordConfirm;

    public ChangeUserPasswordRequest(@NotNull String previousPassword, @NonNull String password, @NonNull String passwordConfirm) {
        this.previousPassword = previousPassword;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }
}
