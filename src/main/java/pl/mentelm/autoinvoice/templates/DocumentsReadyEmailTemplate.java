package pl.mentelm.autoinvoice.templates;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class DocumentsReadyEmailTemplate implements EmailTemplate {

    private final LocalDate startOfPreviousMonth;
    private final String folderUrl;

    @Override
    public String getSubject() {
        return "Nowe dokumenty dostępne do pobrania";
    }

    @Override
    public String getTemplateName() {
        return "documents-ready";
    }

    @Override
    public Map<String, Object> getContextMap(Locale locale) {
        return Map.of(
                "title", getSubject(),
                "month", startOfPreviousMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, locale),
                "year", startOfPreviousMonth.getYear(),
                "folderUrl", folderUrl
        );
    }
}
