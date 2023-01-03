package pl.mentelm.autoinvoice.fakturownia;

import lombok.Value;

@Value
public class InvoiceInfo {
    long id;
    String number;
    String token;
}
