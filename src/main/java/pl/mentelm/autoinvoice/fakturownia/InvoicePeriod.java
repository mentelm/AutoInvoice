package pl.mentelm.autoinvoice.fakturownia;

public enum InvoicePeriod {
    THIS_MONTH,
    LAST_MONTH;

    public String getParamText() {
        return name().toLowerCase();
    }
}
