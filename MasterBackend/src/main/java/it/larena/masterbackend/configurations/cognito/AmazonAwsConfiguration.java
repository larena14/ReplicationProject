package it.larena.masterbackend.configurations.cognito;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "aws", ignoreUnknownFields = false)
public class AmazonAwsConfiguration {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    private final Cognito cognito = new Cognito();


    @Getter
    @Setter
    public static class Cognito {
        @Value("${aws.cognito.user-pool-id}")
        private String userPoolId;

        @Value("${aws.cognito.app-client-id}")
        private String appClientId;

        @Value("${aws.cognito.app-client-secret}")
        private String appClientSecret;
    }

}
