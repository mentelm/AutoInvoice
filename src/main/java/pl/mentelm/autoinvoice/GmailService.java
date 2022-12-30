package pl.mentelm.autoinvoice;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.mentelm.autoinvoice.configuration.AutoInvoiceConfigurationProperties;
import pl.mentelm.autoinvoice.summary.SummaryContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class GmailService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final String FILTER_PATTERN = "label:%s has:attachment after:%s before:%s";

    private final Gmail gmail;
    private final AutoInvoiceConfigurationProperties properties;

    public List<Message> getMessagesForLabel(String label, LocalDate from, LocalDate to) {
        String formattedFrom = FORMATTER.format(from); // inclusive
        String formattedTo = FORMATTER.format(to); // exclusive
        String filter = FILTER_PATTERN.formatted(label, formattedFrom, formattedTo);

        try {
            List<Message> messages = gmail.users().messages()
                    .list(properties.getUserId())
                    .setQ(filter)
                    .execute()
                    .getMessages();

            SummaryContextHolder.setMessageCount(messages.size());
            log.info("Found {} messages with label {} between {} and {}",
                    messages.size(), label, formattedFrom, formattedTo);

            return messages;
        } catch (IOException e) {
            throw new RuntimeException("Could not fetch messages", e);
        }
    }

    @SneakyThrows
    public Stream<BinaryData> getAttachments(Message _message) {
        Message message = gmail.users().messages().get(properties.getUserId(), _message.getId()).execute();

        if (message.getPayload().getBody().getSize() > 0) {
            // do your work with:
            MessagePartBody body = message.getPayload().getBody();
//            new Attachment(body.getAttachmentId(), body.getSize(), body.decodeData());

            log.warn("BODY (not-implemented) {} (size {}): {}", body.getAttachmentId(), body.getSize(), body.getData());
            return Stream.empty();
        }


        // multipart
        List<MessagePart> parts = message.getPayload().getParts();

        if (parts == null) {
            return Stream.empty();
        }

        return parts.stream()
                .filter(part -> StringUtils.isNotBlank(part.getFilename()))
                .map(part -> toBinaryData(message, part));
    }

    @SneakyThrows
    private BinaryData toBinaryData(Message message, MessagePart part) {
        String attId = part.getBody().getAttachmentId();
        MessagePartBody attachPart = gmail.users().messages().attachments().get(properties.getUserId(), message.getId(), attId).execute();

        return new BinaryData(
                getFilename(part),
                "application/pdf",
                attachPart.decodeData());
    }

    private String getFilename(MessagePart part) {
        return part.getFilename()
                .replaceAll("/", "-")
                .replaceAll(" ", "_");
    }

    @SneakyThrows
    public void createAndSendEmail(String subject, String body) {
        MimeMessage messageContent = createMessageContent(subject, body);
        Message message = sendMail(messageContent);

        gmail.users().messages().send(properties.getUserId(), message).execute();
    }

    @SneakyThrows
    private Message sendMail(MimeMessage emailContent) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    @SneakyThrows
    private MimeMessage createMessageContent(String subject, String body) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress("no-reply@mentelm.pl"));
        email.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(properties.getTargetEmail()));
        email.setSubject(subject);
        email.setText(body, StandardCharsets.UTF_8.name(), "html");
        return email;
    }
}
