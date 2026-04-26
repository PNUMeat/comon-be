package site.codemonster.comon.global.globalConfig;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import site.codemonster.comon.global.error.fcm.FcmCredentialsException;

import java.io.IOException;
import java.util.List;

@Configuration
@Slf4j
public class FcmConfig {

    @Value("${fcm.firebase.config.path}")
    private String firebaseConfigPath;

    @Bean
    public GoogleCredentials googleCredentials() {
        try {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            return googleCredentials;
        } catch (IOException ex) {
            log.error("Firebase Credential not found");
            throw new FcmCredentialsException();
        }
    }

    @Bean
    public FirebaseApp firebaseApp(GoogleCredentials googleCredentials) {

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(googleCredentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }


}
