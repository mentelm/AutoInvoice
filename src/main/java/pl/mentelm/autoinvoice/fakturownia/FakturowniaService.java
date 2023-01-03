package pl.mentelm.autoinvoice.fakturownia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import pl.mentelm.autoinvoice.BinaryData;
import pl.mentelm.autoinvoice.configuration.FakturowniaConfigurationProperties;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FakturowniaService {

    private final FakturowniaApi fakturowniaApi;
    private final FakturowniaConfigurationProperties properties;

    public List<InvoiceInfo> getInvoices(InvoicePeriod period, int page, int pageSize) {
        return fakturowniaApi.getInvoices(period.getParamText(), page, pageSize, properties.getToken());
    }

    public BinaryData downloadInvoice(InvoiceInfo invoiceInfo) {
        log.info("Downloading invoice {} [{}]", invoiceInfo.getNumber(), invoiceInfo.getId());
        byte[] invoiceData = fakturowniaApi.downloadPdf(invoiceInfo.getId(), properties.getToken());
        return new BinaryData(
                invoiceInfo.getNumber().replace("/", "_").concat(".pdf"),
                MediaType.APPLICATION_PDF_VALUE,
                invoiceData);
    }
}
