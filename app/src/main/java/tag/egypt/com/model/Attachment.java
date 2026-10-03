package tag.egypt.com.model;

import java.io.Serializable;

/**
 * Represents a file or image attached to a user message in TAJ EGY.
 */
public final class Attachment implements Serializable {
    private final String id;
    private final String name;
    private final String mimeType;
    private final long sizeBytes;
    private final String uriString;
    private final String base64Data;

    public Attachment(String id, String name, String mimeType, long sizeBytes, String uriString, String base64Data) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.name = name != null ? name : "attachment";
        this.mimeType = mimeType != null ? mimeType : "application/octet-stream";
        this.sizeBytes = sizeBytes;
        this.uriString = uriString != null ? uriString : "";
        this.base64Data = base64Data != null ? base64Data : "";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getUriString() {
        return uriString;
    }

    public String getBase64Data() {
        return base64Data;
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isTextOrCode() {
        if (mimeType == null) return false;
        return mimeType.startsWith("text/") ||
                mimeType.contains("json") ||
                mimeType.contains("xml") ||
                mimeType.contains("javascript") ||
                mimeType.contains("python") ||
                mimeType.contains("kotlin") ||
                mimeType.contains("markdown");
    }

    public String getFormattedSize() {
        if (sizeBytes < 1024) return sizeBytes + " B";
        if (sizeBytes < 1024 * 1024) return String.format(java.util.Locale.US, "%.1f KB", sizeBytes / 1024.0);
        return String.format(java.util.Locale.US, "%.1f MB", sizeBytes / (1024.0 * 1024.0));
    }
}
