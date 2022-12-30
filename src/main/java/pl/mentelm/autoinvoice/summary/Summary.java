package pl.mentelm.autoinvoice.summary;

import lombok.Data;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Summary {
    String monthFolder;
    String sharingUrl;
    int messageCount;
    AtomicInteger attachmentCount = new AtomicInteger();
    long runTimeMillis;
    LocalDate startDate;
    LocalDate endDate;
}
