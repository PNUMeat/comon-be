package site.codemonster.comon.global.globalConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    private final AwsProperties awsProperties;

    @Autowired
    public S3Config(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(Region.of(awsProperties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        awsProperties.getAccessKey(),
                        awsProperties.getSecretKey()
                    )
                )
            )
            .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
            .region(Region.of(awsProperties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        awsProperties.getAccessKey(),
                        awsProperties.getSecretKey()
                    )
                )
            )
            .build();
    }
}
