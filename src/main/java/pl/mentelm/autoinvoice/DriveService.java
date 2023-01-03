package pl.mentelm.autoinvoice;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.mentelm.autoinvoice.summary.SummaryContextHolder;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriveService {

    private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
    private final Drive drive;

    @SneakyThrows
    public File createFolder(String name, String parentId) {
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType(MIME_TYPE_FOLDER);
        fileMetadata.setParents(List.of(parentId));

        File file = drive.files().create(fileMetadata)
                .setFields("id")
                .execute();
        log.info("Folder {} created ({})", fileMetadata.getName(), file.getId());
        return file;
    }

    @SneakyThrows
    public File createFile(File folder, BinaryData data) {
        File fileMetadata = new File();
        fileMetadata.setName(data.filename());
        fileMetadata.setParents(List.of(folder.getId()));
        InputStreamContent mediaContent = new InputStreamContent(data.type(), new ByteArrayInputStream(data.content()));

        File file = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute();

        SummaryContextHolder.incrementAttachments();
        log.info("File {} uploaded {}", data.filename(), file.getId());
        return file;
    }

    public File findOrCreateFolder(String name) {
        return findOrCreateFolder(name, "root");
    }

    public File findOrCreateFolder(String name, File parent) {
        return findOrCreateFolder(name, parent.getId());
    }

    @SneakyThrows
    public File findOrCreateFolder(String name, String parentId) {
        FileList result = drive.files().list()
                .setQ("'%s' in parents and name = '%s'".formatted(parentId, name))
                .execute();

        log.info("Following files found for name {} with parent {}: {}", name, parentId, result.getFiles().stream().map(File::getName).toList());

        if (result.getFiles().isEmpty()) {
            return createFolder(name, parentId);
        }
        return result.getFiles().get(0);
    }

    @SneakyThrows
    public void setShared(File file) {
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("reader");
        drive.permissions().create(file.getId(), permission).execute();
    }
}
