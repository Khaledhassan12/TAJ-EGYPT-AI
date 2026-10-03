package tag.egypt.com.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reusable prompt template with parameterized variable substitution in TAJ EGY.
 */
public final class PromptTemplate {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)\\}\\}");

    private final String id;
    private final String title;
    private final String description;
    private final String systemPrompt;
    private final String userTemplate;
    private final String tags;
    private final boolean isFavorite;

    public PromptTemplate(
            String id,
            String title,
            String description,
            String systemPrompt,
            String userTemplate,
            String tags,
            boolean isFavorite
    ) {
        this.id = id != null ? id : java.util.UUID.randomUUID().toString();
        this.title = title != null ? title : "Untitled Prompt";
        this.description = description != null ? description : "";
        this.systemPrompt = systemPrompt != null ? systemPrompt : "";
        this.userTemplate = userTemplate != null ? userTemplate : "";
        this.tags = tags != null ? tags : "";
        this.isFavorite = isFavorite;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getUserTemplate() {
        return userTemplate;
    }

    public String getTags() {
        return tags;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public List<String> extractVariables() {
        List<String> vars = new ArrayList<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(userTemplate);
        while (matcher.find()) {
            String varName = matcher.group(1);
            if (!vars.contains(varName)) {
                vars.add(varName);
            }
        }
        return Collections.unmodifiableList(vars);
    }

    public String render(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return userTemplate;
        }
        String rendered = userTemplate;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            rendered = rendered.replace(placeholder, entry.getValue());
        }
        return rendered;
    }
}
