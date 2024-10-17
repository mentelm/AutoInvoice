package pl.mentelm.autoinvoice.pipelines;

import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.mentelm.autoinvoice.BinaryData;
import pl.mentelm.autoinvoice.google.DriveService;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public abstract class InvoicePipeline {

    protected final DriveService driveService;

    public abstract void run(File targetFolder);

    protected <M> int run(Supplier<? extends Collection<M>> metadataSupplier,
                       Function<M, Stream<BinaryData>> fetchingFunction,
                       File targetFolder) {
        Collection<M> metadata = metadataSupplier.get();

        List<BinaryData> invoiceBinaries = metadata.stream()
                .flatMap(fetchingFunction)
                .toList();

        invoiceBinaries.forEach(inv -> {
            File uploaded = driveService.createFile(targetFolder, inv);
            driveService.setShared(uploaded);
        });

        return invoiceBinaries.size();
    }
}
