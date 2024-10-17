package pl.mentelm.autoinvoice.summary;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SummaryContextHolder {

    private final ThreadLocal<Summary> builder = new ThreadLocal<>();

    public void initialize() {
        builder.set(new Summary());
    }

    public void setMessageCount(int count) {
        builder.get().setMessageCount(count);
    }

    public void addAttachments(int count) {
        builder.get().getAttachmentCount().addAndGet(count);
    }

    public void addOutgoingInvoices(int count) {
        builder.get().getOutgoingInvoiceCount().addAndGet(count);
    }

    public Summary get() {
        return builder.get();
    }

    public Summary getAndRemove() {
        Summary summary = builder.get();
        builder.remove();
        return summary;
    }
}
