package pl.mentelm.autoinvoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.mentelm.autoinvoice.google.GmailService;
import pl.mentelm.autoinvoice.templates.EmailTemplate;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Locale LOCALE = new Locale("pl");

    private final TemplateEngine templateEngine;
    private final GmailService gmailService;

    public void send(EmailTemplate emailTemplate) {
        Context context = new Context(LOCALE, emailTemplate.getContextMap(LOCALE));
        String body = templateEngine.process(emailTemplate.getTemplateName(), context);

        gmailService.createAndSendEmail(emailTemplate.getSubject(), body);
        log.info("Email \"{}\" sent", emailTemplate.getSubject());
    }
}
