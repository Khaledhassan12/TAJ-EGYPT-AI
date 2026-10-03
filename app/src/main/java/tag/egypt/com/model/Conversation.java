package tag.egypt.com.model;

/**
 * Encapsulates a persistent conversation session in TAJ EGY.
 */
public final class Conversation {
    private final String id;
    private final String title;
    private final long createdAt;
    private final long updatedAt;
    private final String providerId;
    private final String modelId;
    private final String systemPrompt;
    private final boolean isPinned;
    private final boolean isArchived;
    private final int messageCount;

    public Conversation(
            String id,
            String title,
            long createdAt,
            long updatedAt,
            String providerId,
            String modelId,
            String systemPrompt,
            boolean isPinned,
            boolean isArchived,
            int messageCount
    ) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.title = (title != null && !title.trim().isEmpty()) ? title : "New Chat";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt > 0 ? updatedAt : System.currentTimeMillis();
        this.providerId = providerId != null ? providerId : "";
        this.modelId = modelId != null ? modelId : "";
        this.systemPrompt = systemPrompt != null ? systemPrompt : "";
        this.isPinned = isPinned;
        this.isArchived = isArchived;
        this.messageCount = messageCount;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getModelId() {
        return modelId;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public Conversation copyWithTitle(String newTitle) {
        return new Conversation(
                this.id,
                newTitle,
                this.createdAt,
                System.currentTimeMillis(),
                this.providerId,
                this.modelId,
                this.systemPrompt,
                this.isPinned,
                this.isArchived,
                this.messageCount
        );
    }

    public Conversation copyWithPinned(boolean pinned) {
        return new Conversation(
                this.id,
                this.title,
                this.createdAt,
                this.updatedAt,
                this.providerId,
                this.modelId,
                this.systemPrompt,
                pinned,
                this.isArchived,
                this.messageCount
        );
    }

    public Conversation copyWithArchived(boolean archived) {
        return new Conversation(
                this.id,
                this.title,
                this.createdAt,
                this.updatedAt,
                this.providerId,
                this.modelId,
                this.systemPrompt,
                this.isPinned,
                archived,
                this.messageCount
        );
    }

    public Conversation copyWithModel(String newProviderId, String newModelId) {
        return new Conversation(
                this.id,
                this.title,
                this.createdAt,
                System.currentTimeMillis(),
                newProviderId,
                newModelId,
                this.systemPrompt,
                this.isPinned,
                this.isArchived,
                this.messageCount
        );
    }
}
