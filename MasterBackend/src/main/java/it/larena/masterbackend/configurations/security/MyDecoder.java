package it.larena.masterbackend.configurations.security;

import it.larena.masterbackend.exceptions.InvalidTokenException;
import it.larena.masterbackend.services.CognitoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

// Just a comment

@Component
public class MyDecoder implements JwtDecoder {

    @Value("${spring.security.oauth2.client.provider.cognito.issuerUri}")
    private String issuer;

    @Autowired
    private CognitoService cognitoService;


    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            if(!cognitoService.isTokenValid(token))
                throw new InvalidTokenException("Invalid token.");
        }
        catch (Exception e){
            throw new InvalidTokenException("Invalid token.");
        }
        return JwtDecoders.fromIssuerLocation(issuer).decode(token);
    }
}