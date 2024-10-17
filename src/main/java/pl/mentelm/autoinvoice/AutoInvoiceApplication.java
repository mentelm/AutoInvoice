package pl.mentelm.autoinvoice;

import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import pl.mentelm.autoinvoice.configuration.AutoInvoiceConfigurationProperties;
import pl.mentelm.autoinvoice.google.DriveService;
import pl.mentelm.autoinvoice.pipelines.InvoicePipeline;
import pl.mentelm.autoinvoice.summary.Summarize;
import pl.mentelm.autoinvoice.summary.SummaryContextHolder;
import pl.mentelm.autoinvoice.templates.DocumentsReadyEmailTemplate;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties
@Service
public class AutoInvoiceApplication {

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");

    private final Collection<InvoicePipeline> invoicePipelines;
    private final DriveService driveService;
    private final EmailService emailService;
    private final AutoInvoiceConfigurationProperties properties;

    @Summarize
    public void run() {
        LocalDate today = LocalDate.now(Clock.systemDefaultZone());
        LocalDate startOfPreviousMonth = today.withDayOfMonth(1).minusMonths(1);

        File dirMonth = getOrCreateFolderForMonth(today.minusMonths(1));

        invoicePipelines.forEach(pipeline -> pipeline.run(dirMonth));

        String folderUrl = "https://drive.google.com/drive/folders/%s?usp=sharing".formatted(dirMonth.getId());
        emailService.send(new DocumentsReadyEmailTemplate(startOfPreviousMonth, folderUrl));
        SummaryContextHolder.get().setSharingUrl(folderUrl);
        log.info("Completed. {}", folderUrl);
    }

    private File getOrCreateFolderForMonth(LocalDate date) {
        String year = YEAR_FORMATTER.format(date);
        String month = MONTH_FORMATTER.format(date);
        File dirInvoices = driveService.findOrCreateFolder(properties.getBaseFolderName());
        File dirYear = driveService.findOrCreateFolder(year, dirInvoices);
        File dirMonth = driveService.findOrCreateFolder(month, dirYear);

        SummaryContextHolder.get()
                .setMonthFolder("%s/%s/%s".formatted(properties.getBaseFolderName(), year, month));
        return dirMonth;
    }
}
