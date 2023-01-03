package pl.mentelm.autoinvoice.summary;

import lombok.Data;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Summary {
    private String monthFolder;
    private String sharingUrl;
    private int messageCount;
    private AtomicInteger attachmentCount = new AtomicInteger();
    private AtomicInteger outgoingInvoiceCount = new AtomicInteger();
    private long runTimeMillis;
    private LocalDate startDate;
    private LocalDate endDate;
}
