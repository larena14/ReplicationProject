package it.larena.masterbackend.controllers.rest;

import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.ChangePasswordResult;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import it.larena.masterbackend.controllers.requests.ChangeUserPasswordRequest;
import it.larena.masterbackend.controllers.requests.LoginRequest;
import it.larena.masterbackend.controllers.requests.UserSignUpRequest;
import it.larena.masterbackend.controllers.responses.AuthenticationResponse;
import it.larena.masterbackend.exceptions.HashingException;
import it.larena.masterbackend.services.CognitoService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/auth")
public class AuthController {

    private final CognitoService cognitoService;

    public AuthController(CognitoService userService) {
        this.cognitoService = userService;
    }

    @PostMapping(value = "/sign-up")
    public ResponseEntity<String> signUp(@RequestBody @Validated UserSignUpRequest userSignUpRequest) {
        try{
            UserType result = cognitoService.createUser(userSignUpRequest);
            return new ResponseEntity<>("User account <"+result.getUsername()+"> created successfully.", HttpStatus.OK);
        }catch (AWSCognitoIdentityProviderException e){
            return new ResponseEntity<>(e.getMessage()+"\n"+
                    """
                    Remember!\s
                    Password:\s
                    - must have length between 8 and 16 characters;\s
                    - must have at least one upper-case character;\s
                    - must have at least one lower-case character;\s
                    - must have at least one digit character;\s
                    - must have at least one symbol (special character);\s
                    - must not have whitespaces
                    """, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Validated LoginRequest loginRequest) {
        try {
            AuthenticationResponse response = cognitoService.authenticate(loginRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (AWSCognitoIdentityProviderException e){
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }catch (HashingException e){
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .message("Hashing error.")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .message("Error.") // replace with error
                    .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestHeader("Authorization") String accessToken, @RequestBody @Validated ChangeUserPasswordRequest changeUserPasswordRequest) {
        if (accessToken != null) {
            if (accessToken.contains("Bearer "))
                accessToken = accessToken.replace("Bearer ", "");
            try{
                ChangePasswordResult changePasswordResult = cognitoService.changePassword(accessToken, changeUserPasswordRequest);
                return new ResponseEntity<>("Password is changed.", HttpStatus.OK);
            }catch (AWSCognitoIdentityProviderException e){
                return new ResponseEntity<>(e.getMessage()+"\n"+"""
                    Password:\s
                    - must have length between 8 and 16 characters;\s
                    - must have at least one upper-case character;\s
                    - must have at least one lower-case character;\s
                    - must have at least one digit character;\s
                    - must have at least one symbol (special character);\s
                    - must not have whitespaces
                    Password and confirm password must be equals.
                    """, HttpStatus.BAD_REQUEST);
            }catch (HashingException e){
                return new ResponseEntity<>("Hashing error.", HttpStatus.BAD_REQUEST);
            }catch (Exception e){
                return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Header not correct.", HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken) {
        if (accessToken != null) {
            if (accessToken.contains("Bearer "))
                accessToken = accessToken.replace("Bearer ", "");
            try{
                cognitoService.logout(accessToken);
                return new ResponseEntity<>("Log out complete.", HttpStatus.OK);
            }catch (AWSCognitoIdentityProviderException e){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }catch (Exception e){
                return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Header not correct.", HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam @NotNull @NotBlank @Email String email) {
        try{
            ForgotPasswordResult result = cognitoService.userForgotPassword(email);
            return new ResponseEntity<>(
                    result.getCodeDeliveryDetails().getDestination()+"\nYou should soon receive an email which will allow you to reset your password. Check your spam and trash if you can't find the email.", HttpStatus.OK);
        }catch (AWSCognitoIdentityProviderException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (HashingException e){
            return new ResponseEntity<>("Hashing error", HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/test")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Online.", HttpStatus.OK);
    }

}
