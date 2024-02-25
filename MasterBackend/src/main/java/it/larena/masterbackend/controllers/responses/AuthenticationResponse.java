package it.larena.masterbackend.controllers.responses;

import lombok.*;
import java.io.Serializable;

@Data
@Builder
public class AuthenticationResponse implements Serializable {

    private String username;

    private String accessToken;

    private String idToken;

    private String refreshToken;

    private String sessionId;

    private String challengeType;

    private String message;

}
