package pl.mentelm.autoinvoice.fakturownia;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum InvoicePeriod {
    LAST_MONTH;

    public String getParamText() {
        return name().toLowerCase();
    }
}
