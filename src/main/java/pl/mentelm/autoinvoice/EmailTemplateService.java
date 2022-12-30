package pl.mentelm.autoinvoice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private static final Locale LOCALE = new Locale("pl");

    private final TemplateEngine templateEngine;

    public String getBody(String subject, LocalDate date, String folderUrl) {
        Context context = new Context(LOCALE, Map.of(
                "title", subject,
                "month", date.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, LOCALE),
                "year", date.getYear(),
                "folderUrl", folderUrl
        ));
        return templateEngine.process("documents-ready", context);
    }
}
