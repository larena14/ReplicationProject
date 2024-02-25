package it.larena.masterbackend.configurations.cognito;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class AmazonCognitoConfiguration {

    private final AmazonAwsConfiguration amazonAwsConfiguration;


    @Bean
    public AWSCognitoIdentityProvider awsCognitoIdentityProviderClient() {

        return AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(amazonAwsConfiguration.getAccessKey(), amazonAwsConfiguration.getSecretKey())))
                .withRegion(amazonAwsConfiguration.getRegion())
                .build();
    }


}
