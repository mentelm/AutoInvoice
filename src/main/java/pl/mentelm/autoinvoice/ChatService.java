package pl.mentelm.autoinvoice;

import com.google.api.client.json.JsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.mentelm.autoinvoice.configuration.AutoInvoiceConfigurationProperties;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AutoInvoiceConfigurationProperties properties;
    private final JsonFactory jsonFactory;
    private final HttpClient client = HttpClient.newHttpClient();

    @SneakyThrows
    public void sendChatNotification(String content) {
        String message = jsonFactory.toString(Map.of("text", content));

        HttpRequest request = HttpRequest.newBuilder(URI.create(properties.getChatWebhook()))
                .header("accept", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            log.warn("Unexpected status code {}!\n{}", response.statusCode(), response.body());
        }
    }
}
