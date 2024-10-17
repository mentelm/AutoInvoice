package pl.mentelm.autoinvoice.pipelines;

import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Component;
import pl.mentelm.autoinvoice.configuration.AutoInvoiceConfigurationProperties;
import pl.mentelm.autoinvoice.google.DriveService;
import pl.mentelm.autoinvoice.google.GmailService;
import pl.mentelm.autoinvoice.summary.SummaryContextHolder;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class GmailAttachmentsInvoicePipeline extends InvoicePipeline {

    private final GmailService gmailService;
    private final AutoInvoiceConfigurationProperties properties;

    public GmailAttachmentsInvoicePipeline(DriveService driveService,
                                           GmailService gmailService,
                                           AutoInvoiceConfigurationProperties properties) {
        super(driveService);
        this.gmailService = gmailService;
        this.properties = properties;
    }

    @Override
    public void run(File targetFolder) {
        LocalDate today = LocalDate.now(Clock.systemDefaultZone());
        LocalDate startOfPreviousMonth = today.withDayOfMonth(1).minusMonths(1);
        LocalDate startOfCurrentMonth = today.withDayOfMonth(1);

        int attachmentCount = super.run(
                () -> gmailService.getMessagesForLabel(properties.getWantedLabel(), startOfPreviousMonth, startOfCurrentMonth),
                gmailService::getAttachments,
                targetFolder
        );
        SummaryContextHolder.addAttachments(attachmentCount);
    }
}
