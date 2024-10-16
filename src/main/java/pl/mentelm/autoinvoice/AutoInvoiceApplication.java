package pl.mentelm.autoinvoice;

import com.google.api.services.drive.model.File;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import pl.mentelm.autoinvoice.configuration.AutoInvoiceConfigurationProperties;
import pl.mentelm.autoinvoice.fakturownia.FakturowniaService;
import pl.mentelm.autoinvoice.fakturownia.InvoiceInfo;
import pl.mentelm.autoinvoice.fakturownia.InvoicePeriod;
import pl.mentelm.autoinvoice.summary.Summarize;
import pl.mentelm.autoinvoice.summary.SummaryContextHolder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties
@Service
public class AutoInvoiceApplication {

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final int PAGE_SIZE = 10;

    private static final String EMAIL_SUBJECT = "Nowe dokumenty dostępne do pobrania";

    private final GmailService gmailService;
    private final DriveService driveService;
    private final FakturowniaService fakturowniaService;
    private final EmailTemplateService emailTemplateService;
    private final AutoInvoiceConfigurationProperties properties;

    @Summarize
    public void run() {
        LocalDate today = LocalDate.now(Clock.systemDefaultZone());
        LocalDate startOfPreviousMonth = today.withDayOfMonth(1).minusMonths(1);
        LocalDate startOfCurrentMonth = today.withDayOfMonth(1);

        SummaryContextHolder.get().setStartDate(startOfPreviousMonth);
        SummaryContextHolder.get().setEndDate(startOfCurrentMonth);

        File dirMonth = getOrCreateFolderForMonth(startOfPreviousMonth);
        getAndUploadInvoices(startOfPreviousMonth, startOfCurrentMonth, dirMonth);
        driveService.setShared(dirMonth);

        renderAndSendEmail(startOfPreviousMonth, dirMonth);
    }

    private void renderAndSendEmail(LocalDate startOfPreviousMonth, File sharedFolder) {
        String folderUrl = "https://drive.google.com/drive/folders/".concat(sharedFolder.getId()).concat("?usp=sharing");
        String body = emailTemplateService.getBody(EMAIL_SUBJECT, startOfPreviousMonth, folderUrl);

        gmailService.createAndSendEmail(EMAIL_SUBJECT, body);
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

    private void getAndUploadInvoices(LocalDate fromDate, LocalDate toDate, File targetFolder) {
        List<Message> messages = gmailService.getMessagesForLabel(properties.getWantedLabel(), fromDate, toDate);
        messages.stream()
                .flatMap(gmailService::getAttachments)
                .map(att -> {
                    File uploadedAttachment = driveService.createFile(targetFolder, att);
                    SummaryContextHolder.incrementAttachments();
                    return uploadedAttachment;
                })
                .forEach(driveService::setShared);

        File fakturowniaFolder = driveService.findOrCreateFolder("fakturownia", targetFolder);
        int currentPage = 1;
        int lastPageSize;
        do {
            List<InvoiceInfo> invoices = fakturowniaService.getInvoices(InvoicePeriod.LAST_MONTH, currentPage++, PAGE_SIZE);

            invoices.stream()
                    .map(fakturowniaService::downloadInvoice)
                    .map(inv -> driveService.createFile(fakturowniaFolder, inv))
                    .forEach(driveService::setShared);
            SummaryContextHolder.addOutgoingInvoices(invoices.size());

            lastPageSize = invoices.size();
        } while (lastPageSize == PAGE_SIZE);
        driveService.setShared(fakturowniaFolder);
    }
}
