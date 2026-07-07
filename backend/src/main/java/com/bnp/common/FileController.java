package com.bnp.common;

import com.bnp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Real multipart upload endpoint. Authenticated users POST a file and get back a
 * public URL they can then save on their avatar / document / company record.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService storage;
    private final CurrentUser currentUser;

    /**
     * @param category logical bucket (avatars, documents, logos) — defaults to misc.
     * @return {@code { url, fileName, contentType, size }}
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "misc") String category) {
        // Touch currentUser so the upload is tied to an authenticated session.
        currentUser.id();
        String url = storage.store(file, category);
        return ApiResponse.ok("File uploaded", Map.of(
                "url", url,
                "fileName", file.getOriginalFilename() == null ? "" : file.getOriginalFilename(),
                "contentType", file.getContentType() == null ? "" : file.getContentType(),
                "size", file.getSize()));
    }
}
