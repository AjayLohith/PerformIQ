package com.performiq.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ChecksumUtil {

    private ChecksumUtil() {}

    public static String calculateSha256(Path filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[65536];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
