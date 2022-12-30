package pl.mentelm.autoinvoice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "autoinvoice")
public class AutoInvoiceConfigurationProperties {

    String baseFolderName;
    String wantedLabel;
    String userEmail;
    String userId;
    String targetEmail;
    String credentialsB64;
    String chatWebhook;
}
