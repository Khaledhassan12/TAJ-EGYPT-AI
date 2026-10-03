package tag.egypt.com.network;

import tag.egypt.com.model.ChatStreamEvent;

/**
 * Callback interface invoked during AI streaming responses in TAJ EGY.
 */
public interface StreamCallback {
    void onEvent(ChatStreamEvent event);
}
