package pl.mentelm.autoinvoice.fakturownia;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "fakturowniaApiClient",
        url = "${fakturownia.base-domain}")
public interface FakturowniaApi {

    @GetMapping(path = "invoices.json",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<InvoiceInfo> getInvoices(@RequestParam("period") String period,
                                  @RequestParam("page") int page,
                                  @RequestParam("per_page") int pageSize,
                                  @RequestParam("api_token") String token);

    @GetMapping(path = "invoices/{id}.pdf",
            produces = MediaType.APPLICATION_PDF_VALUE)
    byte[] downloadPdf(@PathVariable("id") long id,
                       @RequestParam("api_token") String token);
}
