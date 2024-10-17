package pl.mentelm.autoinvoice.pipelines;

import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Component;
import pl.mentelm.autoinvoice.fakturownia.FakturowniaService;
import pl.mentelm.autoinvoice.fakturownia.InvoiceInfo;
import pl.mentelm.autoinvoice.fakturownia.InvoicePeriod;
import pl.mentelm.autoinvoice.google.DriveService;
import pl.mentelm.autoinvoice.summary.SummaryContextHolder;

import java.util.ArrayList;
import java.util.List;

@Component
public class OutgoingInvoicePipeline extends InvoicePipeline {

    private static final int PAGE_SIZE = 10;

    private final FakturowniaService fakturowniaService;

    public OutgoingInvoicePipeline(DriveService driveService,
                                   FakturowniaService fakturowniaService) {
        super(driveService);
        this.fakturowniaService = fakturowniaService;
    }

    public void run(File targetFolder) {
        File fakturowniaFolder = driveService.findOrCreateFolder("fakturownia", targetFolder);
        int outgoingCount = super.run(
                this::getAllInvoiceInfo,
                fakturowniaService::downloadInvoice,
                fakturowniaFolder
        );
        driveService.setShared(fakturowniaFolder);
        driveService.setShared(targetFolder);
        SummaryContextHolder.addOutgoingInvoices(outgoingCount);
    }


    private List<InvoiceInfo> getAllInvoiceInfo() {
        List<InvoiceInfo> invoices = new ArrayList<>();
        int currentPage = 1;
        int lastPageSize;
        do {
            List<InvoiceInfo> fetched = fakturowniaService.getInvoices(InvoicePeriod.LAST_MONTH, currentPage++, PAGE_SIZE);
            lastPageSize = fetched.size();
            invoices.addAll(fetched);
        } while (lastPageSize == PAGE_SIZE);
        return invoices;
    }
}
