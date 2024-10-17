package pl.mentelm.autoinvoice.summary;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import pl.mentelm.autoinvoice.google.ChatService;

@Aspect
@Component
@RequiredArgsConstructor
public class SummaryAspect {

    private static final String SUMMARY_TEMPLATE = """
            AutoInvoice run performed in %s seconds.
            %s -> %s
            Found %d attachments in %d messages
            %d outgoing invoices copied from Fakturownia
            Uploaded to <%s|folder %s>
            """;


    private final ChatService chatService;

    @Around("@annotation(pl.mentelm.autoinvoice.summary.Summarize)")
    public Object summarize(ProceedingJoinPoint joinPoint) throws Throwable {
        SummaryContextHolder.initialize();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        var result = joinPoint.proceed();
        stopWatch.stop();
        SummaryContextHolder.get().setRunTimeMillis(stopWatch.getLastTaskInfo().getTimeMillis());
        Summary summary = SummaryContextHolder.getAndRemove();
        chatService.sendChatNotification(generateChatMessage(summary));

        return result;
    }
    private String generateChatMessage(Summary summary) {
        return SUMMARY_TEMPLATE.formatted(
                toSeconds(summary.getRunTimeMillis()),
                summary.getStartDate(), summary.getEndDate(),
                summary.getAttachmentCount().intValue(), summary.getMessageCount(),
                summary.getOutgoingInvoiceCount().intValue(),
                summary.getSharingUrl(), summary.getMonthFolder()
        );
    }

    private double toSeconds(double millis) {
        return millis / 1000;
    }
}
