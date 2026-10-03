package tag.egypt.com.tools;

/**
 * Universal interface for callable tools in TAJ EGY.
 */
public interface Tool {
    String getName();
    String getDescription();
    String getInputSchemaJson();
    String execute(String argumentsJson) throws Exception;
}
