package tag.egypt.com.security;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * Validates inputs, URLs, file paths, and archive contents against vulnerabilities in TAJ EGY.
 */
public final class SecurityValidator {

    private static final Pattern PRIVATE_IP_PATTERN = Pattern.compile(
            "^(127\\.|10\\.|192\\.168\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.|0\\.0\\.0\\.0|localhost).*"
    );

    private SecurityValidator() {
        // Utility
    }

    public static String validateAndNormalizeUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        String trimmed = rawUrl.trim();
        if (trimmed.contains("://")) {
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                throw new IllegalArgumentException("Unsupported protocol. Only HTTP and HTTPS are permitted.");
            }
        } else {
            trimmed = "https://" + trimmed;
        }

        try {
            URI uri = new URI(trimmed).normalize();
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Unsupported protocol: " + scheme + ". Only HTTP and HTTPS are permitted.");
            }

            if (uri.getHost() == null || uri.getHost().trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid host in URL: " + trimmed);
            }

            String normalized = uri.toString();
            while (normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }

            return normalized;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed URL syntax: " + e.getMessage(), e);
        }
    }

    public static boolean isPrivateOrLocalHost(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return false;
            return PRIVATE_IP_PATTERN.matcher(host.toLowerCase()).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static File validateZipEntry(File destinationDir, ZipEntry zipEntry) throws IOException {
        String entryName = zipEntry.getName();
        File targetFile = new File(destinationDir, entryName);
        String canonicalDest = destinationDir.getCanonicalPath();
        String canonicalTarget = targetFile.getCanonicalPath();

        if (!canonicalTarget.startsWith(canonicalDest + File.separator) && !canonicalTarget.equals(canonicalDest)) {
            throw new SecurityException("Zip Slip path traversal exploit detected in entry: " + entryName);
        }

        return targetFile;
    }

    public static void validateFileSize(long sizeBytes, long maxSizeBytes) {
        if (sizeBytes > maxSizeBytes) {
            throw new IllegalArgumentException(
                    String.format(
                            java.util.Locale.US,
                            "File size (%.2f MB) exceeds maximum permitted limit (%.2f MB)",
                            sizeBytes / (1024.0 * 1024.0),
                            maxSizeBytes / (1024.0 * 1024.0)
                    )
            );
        }
    }
}
