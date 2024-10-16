package pl.mentelm.autoinvoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableFeignClients
@SpringBootApplication
@EnableAspectJAutoProxy
public class AutoInvoiceStarter {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(AutoInvoiceStarter.class, args);

        ctx.getBean(AutoInvoiceApplication.class).run();
    }
}
