package it.larena.masterbackend.services;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;
import it.larena.masterbackend.configurations.cognito.AmazonAwsConfiguration;
import it.larena.masterbackend.controllers.requests.ChangeUserPasswordRequest;
import it.larena.masterbackend.controllers.requests.LoginRequest;
import it.larena.masterbackend.controllers.requests.UserSignUpRequest;
import it.larena.masterbackend.controllers.responses.AuthenticationResponse;
import it.larena.masterbackend.exceptions.HashingException;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.passay.CharacterData;
import org.passay.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.amazonaws.services.cognitoidp.model.ChallengeNameType.NEW_PASSWORD_REQUIRED;

@RequiredArgsConstructor
@Slf4j
@Service
public class CognitoService {

    private final AWSCognitoIdentityProvider awsCognitoIdentityProvider;
    private final AmazonAwsConfiguration amazonAwsConfiguration;

    public AuthenticationResponse authenticate(LoginRequest loginRequest) throws AWSCognitoIdentityProviderException, HashingException{

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        final Map<String, String> authParams = new HashMap<>();
        authParams.put(CognitoAttributesEnum.USERNAME.name(), username);
        authParams.put(CognitoAttributesEnum.PASSWORD.name(), password);
        authParams.put(CognitoAttributesEnum.SECRET_HASH.name(), calculateSecretHash(amazonAwsConfiguration.getCognito().getAppClientId(), amazonAwsConfiguration.getCognito().getAppClientSecret(), username));

        final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withClientId(amazonAwsConfiguration.getCognito().getAppClientId())
                .withUserPoolId(amazonAwsConfiguration.getCognito().getUserPoolId())
                .withAuthParameters(authParams);

        AdminInitiateAuthResult result = Optional.of(awsCognitoIdentityProvider.adminInitiateAuth(authRequest))
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        if (ObjectUtils.nullSafeEquals(NEW_PASSWORD_REQUIRED.name(), result.getChallengeName())) {
            return AuthenticationResponse.builder()
                    .challengeType(NEW_PASSWORD_REQUIRED.name())
                    .sessionId(result.getSession())
                    .username(username)
                    .build();
        }



        return AuthenticationResponse.builder()
                .accessToken(result.getAuthenticationResult().getAccessToken())
                .idToken(result.getAuthenticationResult().getIdToken())
                .refreshToken(result.getAuthenticationResult().getRefreshToken())
                .sessionId(result.getSession())
                .username(username)
                .build();
    }

    public ChangePasswordResult changePassword(@NotNull String accessToken, ChangeUserPasswordRequest changeUserPasswordRequest) throws AWSCognitoIdentityProviderException{
        String newPassword = changeUserPasswordRequest.getPassword();
        String passwordConfirm = changeUserPasswordRequest.getPasswordConfirm();

        if(!isValid(newPassword)
                || !isValid(passwordConfirm)
                || !newPassword.equals(passwordConfirm)){
            throw new InvalidPasswordException("Invalid password.");
        }

        String previousPassword = changeUserPasswordRequest.getPreviousPassword();

        final ChangePasswordRequest request = new ChangePasswordRequest();
        request.withAccessToken(accessToken)
                .withPreviousPassword(previousPassword)
                .withProposedPassword(newPassword);
        return awsCognitoIdentityProvider.changePassword(request);
    }

    public void logout(@NotNull String accessToken) throws AWSCognitoIdentityProviderException{
        awsCognitoIdentityProvider.globalSignOut(new GlobalSignOutRequest().withAccessToken(accessToken));
    }


    public ForgotPasswordResult userForgotPassword(String username) throws AWSCognitoIdentityProviderException, HashingException{
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.withClientId(amazonAwsConfiguration.getCognito().getAppClientId())
                .withUsername(username)
                .withSecretHash(calculateSecretHash(amazonAwsConfiguration.getCognito().getAppClientId(), amazonAwsConfiguration.getCognito().getAppClientSecret(), username));
        return awsCognitoIdentityProvider.forgotPassword(request);
    }


    public UserType createUser(UserSignUpRequest userSignUpRequest) throws AWSCognitoIdentityProviderException{
        if(!isValid(userSignUpRequest.getPassword()))
            throw new InvalidPasswordException("Invalid password.");
        final AdminCreateUserRequest signUpRequest = new AdminCreateUserRequest()
                .withUserPoolId(amazonAwsConfiguration.getCognito().getUserPoolId())
                // The user's temporary password.
                .withTemporaryPassword(generateValidPassword())
                // Specify "EMAIL" if email will be used to send the welcome message
                .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .withUsername(userSignUpRequest.getUsername())
                .withMessageAction(MessageActionType.SUPPRESS)
                .withUserAttributes(
                        new AttributeType().withName("email").withValue(userSignUpRequest.getEmail()),
                        new AttributeType().withName("email_verified").withValue("true"));

        // create user
        AdminCreateUserResult createUserResult = awsCognitoIdentityProvider.adminCreateUser(signUpRequest);

        // assign the roles
        userSignUpRequest.getRoles().forEach(r -> addUserToGroup(userSignUpRequest.getEmail(), r));

        // set permanent password
        setUserPassword(userSignUpRequest.getEmail(), userSignUpRequest.getPassword());

        return createUserResult.getUser();
    }

    public void addUserToGroup(String username, String groupName) throws AWSCognitoIdentityProviderException{
        AdminAddUserToGroupRequest addUserToGroupRequest = new AdminAddUserToGroupRequest()
                .withGroupName(groupName)
                .withUserPoolId(amazonAwsConfiguration.getCognito().getUserPoolId())
                .withUsername(username);

        awsCognitoIdentityProvider.adminAddUserToGroup(addUserToGroupRequest);
    }


    public void setUserPassword(String username, String password) throws AWSCognitoIdentityProviderException{
        AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest()
                .withUsername(username)
                .withPassword(password)
                .withUserPoolId(amazonAwsConfiguration.getCognito().getUserPoolId())
                .withPermanent(true);

        awsCognitoIdentityProvider.adminSetUserPassword(adminSetUserPasswordRequest);
    }

    private String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) throws HashingException {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new HashingException();
        }
    }

    private String generateValidPassword() {
        PasswordGenerator gen = new PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return "ERRONEOUS_SPECIAL_CHARS";
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        return gen.generatePassword(10, splCharRule, lowerCaseRule,
                upperCaseRule, digitRule);
    }

    public boolean isTokenValid(String token){
        GetUserResult response = awsCognitoIdentityProvider.getUser(new GetUserRequest().withAccessToken(token));
        return response.getSdkHttpMetadata().getHttpStatusCode() == 200;
    }

    @Getter
    private enum CognitoAttributesEnum {

        USERNAME("USERNAME"),
        PASSWORD("PASSWORD"),
        SECRET_HASH("SECRET_HASH"),
        NEW_PASSWORD("NEW_PASSWORD");

        private final String values;

        CognitoAttributesEnum(String val) {
            this.values = val;
        }


        //Lookup table
        private static final Map<String, CognitoAttributesEnum> lookup = new HashMap<>();

        //Populate the lookup
        static {
            for (CognitoAttributesEnum env : CognitoAttributesEnum.values()) {
                lookup.put(env.getValues(), env);
            }
        }

        //This method can be used for reverse lookup purpose
        public static CognitoAttributesEnum get(String key) {
            return lookup.get(key);
        }


    }

    @SneakyThrows
    private boolean isValid(String password) {

        //customizing validation messages
        Properties props = new Properties();
        InputStream inputStream = getClass()
                .getClassLoader().getResourceAsStream("passay.properties");
        props.load(inputStream);
        MessageResolver resolver = new PropertiesMessageResolver(props);

        PasswordValidator validator = new PasswordValidator(resolver, Arrays.asList(

                // length between 8 and 16 characters
                new LengthRule(8, 16),

                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),

                // at least one lower-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),

                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1),

                // at least one symbol (special character)
                new CharacterRule(EnglishCharacterData.Special, 1),

                // no whitespace
                new WhitespaceRule()
        ));

        RuleResult result = validator.validate(new PasswordData(password));
        return result.isValid();

    }
}
