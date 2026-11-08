package io.chanuth.receipts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.chanuth.receipts.storage.ReceiptStorage;
import io.chanuth.receipts.storage.StorageProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ReceiptUploadTest {

    static final Path STORAGE;

    static {
        try {
            STORAGE = Files.createTempDirectory("receipts-test");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static final byte[] PDF = "%PDF-1.7\n1 0 obj << >> endobj\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};
    static final byte[] HTML = "<html><script>alert(document.cookie)</script></html>".getBytes(StandardCharsets.UTF_8);

    @DynamicPropertySource
    static void storage(DynamicPropertyRegistry registry) {
        registry.add("app.storage.location", STORAGE::toString);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JsonMapper jsonMapper;

    private ResultActions upload(MockMultipartFile file, String name, String date, String amount) throws Exception {
        MockMultipartHttpServletRequestBuilder request = multipart("/receipts");
        if (file != null) {
            request.file(file);
        }
        if (name != null) {
            request.param("employeeName", name);
        }
        if (date != null) {
            request.param("expenseDate", date);
        }
        if (amount != null) {
            request.param("amount", amount);
        }
        return mvc.perform(request);
    }

    private ResultActions uploadValid(MockMultipartFile file) throws Exception {
        return upload(file, "Nadia Perera", "2026-11-03", "42.50");
    }

    private String idOf(ResultActions result) throws Exception {
        return jsonMapper.readTree(result.andReturn().getResponse().getContentAsString()).get("id").asString();
    }

    @Test
    void storesAPdfUnderARandomNameWithItsDetails() throws Exception {
        ResultActions result = uploadValid(new MockMultipartFile("file", "taxi.pdf", "application/pdf", PDF))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.employeeName").value("Nadia Perera"))
                .andExpect(jsonPath("$.expenseDate").value("2026-11-03"))
                .andExpect(jsonPath("$.amount").value(42.50))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.originalFilename").value("taxi.pdf"));
        String id = idOf(result);

        assertThat(STORAGE.resolve(id + ".pdf")).exists().hasBinaryContent(PDF);
        assertThat(STORAGE.resolve(id + ".json")).exists();
    }

    @Test
    void trustsTheBytesNotTheNameOrTheContentType() throws Exception {
        String id = idOf(uploadValid(new MockMultipartFile("file", "scan.pdf", "application/pdf", PNG))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/png")));

        assertThat(STORAGE.resolve(id + ".png")).exists();
        assertThat(STORAGE.resolve(id + ".pdf")).doesNotExist();
    }

    @Test
    void acceptsJpeg() throws Exception {
        uploadValid(new MockMultipartFile("file", "lunch.jpeg", "image/jpeg", JPEG))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/jpeg"));
    }

    @Test
    void refusesAnHtmlFileDressedAsAnImage() throws Exception {
        long before = fileCount();
        uploadValid(new MockMultipartFile("file", "receipt.png", "image/png", HTML))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Only PDF, PNG and JPEG files are accepted"));
        assertThat(fileCount()).isEqualTo(before);
    }

    @Test
    void aMissingFileIsA400NotACrash() throws Exception {
        upload(null, "Nadia Perera", "2026-11-03", "42.50")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The part 'file' is required"));
    }

    @Test
    void anEmptyFileIsRefused() throws Exception {
        uploadValid(new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("The file is empty"));
    }

    @Test
    void validatesTheTextFields() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "taxi.pdf", "application/pdf", PDF);
        // A value that cannot even be converted stops the record from being built,
        // so only the conversion error is reported; the other rules have not run yet.
        upload(pdf, " ", "yesterday", "-5")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.expenseDate").value("has an invalid format"))
                .andExpect(jsonPath("$.errors.employeeName").doesNotExist());

        // With a real date, the validation rules run and report every broken field.
        upload(pdf, " ", "2026-11-03", "-5")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.employeeName").exists())
                .andExpect(jsonPath("$.errors.amount").exists());

        upload(pdf, "Nadia Perera", LocalDate.now().plusDays(3).toString(), "10")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.expenseDate").exists());
    }

    @Test
    void servesTheFileWithTheDetectedTypeAndSafeHeaders() throws Exception {
        String id = idOf(uploadValid(new MockMultipartFile("file", "scan.pdf", "application/pdf", PNG)));

        mvc.perform(get("/receipts/{id}/file", id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"receipt-" + id + ".png\""))
                .andExpect(content().bytes(PNG));

        mvc.perform(get("/receipts/{id}/file", id).param("download", "true"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"receipt-" + id + ".png\""));
    }

    @Test
    void anOddFileNameNeverReachesTheDisk() throws Exception {
        String id = idOf(uploadValid(new MockMultipartFile("file", "../../etc/passwd.pdf", "application/pdf", PDF))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename").value("passwd.pdf")));

        assertThat(STORAGE.resolve(id + ".pdf")).exists();
        assertThat(STORAGE.getParent().getParent().resolve("etc")).doesNotExist();
    }

    @Test
    void idsMustBeUuids() throws Exception {
        mvc.perform(get("/receipts/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for id"));
        mvc.perform(get("/receipts/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void listsAndDeletesReceipts() throws Exception {
        String id = idOf(uploadValid(new MockMultipartFile("file", "taxi.pdf", "application/pdf", PDF)));
        mvc.perform(get("/receipts")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(delete("/receipts/{id}", id)).andExpect(status().isNoContent());
        assertThat(STORAGE.resolve(id + ".pdf")).doesNotExist();
        assertThat(STORAGE.resolve(id + ".json")).doesNotExist();
        mvc.perform(delete("/receipts/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void theDetailsSurviveARestart() throws Exception {
        String id = idOf(uploadValid(new MockMultipartFile("file", "taxi.pdf", "application/pdf", PDF)));

        // A brand-new storage object on the same folder, as after a restart.
        var fresh = new ReceiptStorage(new StorageProperties(STORAGE.toString()), jsonMapper);
        assertThat(fresh.find(UUID.fromString(id)).employeeName()).isEqualTo("Nadia Perera");
    }

    private static long fileCount() throws IOException {
        try (Stream<Path> files = Files.list(STORAGE)) {
            return files.count();
        }
    }
}
