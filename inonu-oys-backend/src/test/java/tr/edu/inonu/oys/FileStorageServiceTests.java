package tr.edu.inonu.oys;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import tr.edu.inonu.oys.service.FileStorageService;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTests {
    @TempDir Path uploadDirectory;

    @Test
    void acceptsMatchingPdfAndRejectsSpoofedOrMissingFiles() {
        FileStorageService service = new FileStorageService(uploadDirectory.toString());
        var pdf = new MockMultipartFile("document", "result.pdf", "application/pdf",
                new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31});
        var spoofed = new MockMultipartFile("document", "malware.pdf", "application/pdf",
                "not-a-pdf".getBytes());

        assertThatCode(() -> service.validateDocument(pdf, "Belge", false)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validateDocument(spoofed, "Belge", false))
                .hasMessageContaining("içeriği geçerli");
        assertThatThrownBy(() -> service.validateDocument(null, "Belge", false))
                .hasMessageContaining("zorunludur");
    }
}
