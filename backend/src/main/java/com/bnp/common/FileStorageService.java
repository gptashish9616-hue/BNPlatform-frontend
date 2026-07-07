package com.bnp.common;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded files (avatars, authenticity documents, company logos) on the
 * local filesystem and returns a public URL that {@code WebMvcConfig} serves back.
 * This is real persistence — files land on disk under {@code bnp.upload.dir} and
 * survive restarts. Swap this impl for S3/GCS later without touching callers.
 */
@Service
@Slf4j
public class FileStorageService {

    /** Extensions we accept. Keeps random executables / scripts out of the store. */
    private static final Set<String> ALLOWED = Set.of(
            "png", "jpg", "jpeg", "gif", "webp", "pdf", "doc", "docx");

    /**
     * Fallback for files without a usable filename extension (screenshots, clipboard
     * saves, downloads that drop the extension) — the browser's declared MIME type is
     * still trustworthy, so map it to one of the extensions above instead of rejecting
     * a genuinely valid image outright.
     */
    private static final Map<String, String> EXTENSION_BY_MIME_TYPE = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/gif", "gif",
            "image/webp", "webp",
            "application/pdf", "pdf",
            "application/msword", "doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");

    private final Path root;
    private final String publicPath;

    public FileStorageService(
            @Value("${bnp.upload.dir:uploads}") String dir,
            @Value("${bnp.upload.public-path:/uploads}") String publicPath) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        this.publicPath = publicPath.replaceAll("/$", "");
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(root);
            log.info("File storage ready at {}", root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + root, e);
        }
    }

    /**
     * Saves the given file under {@code <root>/<subdir>/<uuid>.<ext>} and returns
     * the public URL path (e.g. {@code /uploads/documents/ab12.pdf}).
     */
    public String store(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }
        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = StringUtils.getFilenameExtension(original);
        if (ext == null || !ALLOWED.contains(ext.toLowerCase())) {
            ext = EXTENSION_BY_MIME_TYPE.get(file.getContentType());
        }
        if (ext == null || !ALLOWED.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Allowed: " + String.join(", ", ALLOWED));
        }

        String safeSub = (subdir == null ? "misc" : subdir).replaceAll("[^a-zA-Z0-9_-]", "");
        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext.toLowerCase();

        try {
            Path target = root.resolve(safeSub).normalize();
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("Invalid storage path");
            }
            Files.createDirectories(target);
            Path dest = target.resolve(name);
            try (var in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            return publicPath + "/" + safeSub + "/" + name;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file " + original, e);
        }
    }
}
