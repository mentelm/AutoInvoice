package pl.mentelm.autoinvoice.summary;

public abstract class SummaryContextHolder {

    private final static ThreadLocal<Summary> builder = new ThreadLocal<>();

    public static void initialize() {
        builder.set(new Summary());
    }

    public static void setMessageCount(int count) {
        builder.get().setMessageCount(count);
    }

    public static void incrementAttachments() {
        builder.get().getAttachmentCount().incrementAndGet();
    }

    public static Summary get() {
        return builder.get();
    }
}
