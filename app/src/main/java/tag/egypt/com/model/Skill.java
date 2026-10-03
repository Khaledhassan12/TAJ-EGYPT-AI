package tag.egypt.com.model;

/**
 * Encapsulates an AI Skill in TAJ EGY.
 */
public final class Skill {

    public enum Source {
        MANUAL,
        IMPORTED_ZIP,
        PRESET
    }

    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final String instructions;
    private final String triggerHints;
    private final String tags;
    private final int priority;
    private final boolean isEnabled;
    private final Source source;
    private final String filesSummaryJson;

    public Skill(
            String id,
            String name,
            String version,
            String description,
            String instructions,
            String triggerHints,
            String tags,
            int priority,
            boolean isEnabled,
            Source source,
            String filesSummaryJson
    ) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.name = (name != null && !name.trim().isEmpty()) ? name : "Unnamed Skill";
        this.version = version != null ? version : "1.0.0";
        this.description = description != null ? description : "";
        this.instructions = instructions != null ? instructions : "";
        this.triggerHints = triggerHints != null ? triggerHints : "";
        this.tags = tags != null ? tags : "";
        this.priority = priority;
        this.isEnabled = isEnabled;
        this.source = source != null ? source : Source.MANUAL;
        this.filesSummaryJson = filesSummaryJson != null ? filesSummaryJson : "[]";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getTriggerHints() {
        return triggerHints;
    }

    public String getTags() {
        return tags;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Source getSource() {
        return source;
    }

    public String getFilesSummaryJson() {
        return filesSummaryJson;
    }

    public Skill copyWithEnabled(boolean enabled) {
        return new Skill(
                this.id,
                this.name,
                this.version,
                this.description,
                this.instructions,
                this.triggerHints,
                this.tags,
                this.priority,
                enabled,
                this.source,
                this.filesSummaryJson
        );
    }
}
