package pl.mentelm.autoinvoice.configuration;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
class GoogleApiConfiguration {

    private static final String APPLICATION_NAME = "mentelm-autoinvoice";

    @Bean
    JsonFactory jsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    @Bean
    HttpTransport httpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    Credential googleCredential(AutoInvoiceConfigurationProperties properties) throws IOException {
        byte[] credentials = Base64.decodeBase64(properties.getCredentialsB64().getBytes(StandardCharsets.UTF_8));

        return GoogleCredential.fromStream(new ByteArrayInputStream(credentials))
                .createScoped(List.of(
                        GmailScopes.GMAIL_READONLY,
                        GmailScopes.GMAIL_SEND,
                        DriveScopes.DRIVE
                ))
                .createDelegated(properties.getUserEmail());
    }

    @Bean
    Gmail gmail(HttpTransport httpTransport, JsonFactory jsonFactory, Credential googleCredential) {
        return new Gmail.Builder(httpTransport, jsonFactory, googleCredential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Bean
    Drive drive(HttpTransport httpTransport, JsonFactory jsonFactory, Credential googleCredential) {
        return new Drive.Builder(httpTransport, jsonFactory, googleCredential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
