# Supported AI Providers & Adapters

TAJ EGY ships with built-in adapters for all major commercial, open-router, and self-hosted AI models:

| Provider | Endpoint | Key Formats / Header | Streaming Protocol |
| :--- | :--- | :--- | :--- |
| **OpenAI** | `/v1/chat/completions` | `Authorization: Bearer sk-...` | SSE (`choices[0].delta.content`) |
| **Anthropic** | `/v1/messages` | `x-api-key: sk-ant-...` | SSE (`event: content_block_delta`) |
| **Google Gemini** | `/v1beta/models/{m}:streamGenerateContent` | `?key=AIza...` | SSE (`candidates[0].content.parts`) |
| **DeepSeek** | `/chat/completions` | `Authorization: Bearer sk-...` | SSE (`delta.content`, `delta.reasoning_content`) |
| **OpenRouter** | `/api/v1/chat/completions` | `Authorization: Bearer sk-or-...` | SSE (`choices[0].delta.content`) |
| **Qwen** | `/compatible-mode/v1/chat/completions` | `Authorization: Bearer sk-...` | SSE (`choices[0].delta.content`) |
| **Generic OpenAI** | Custom user URL | Configurable Bearer Key | SSE standard |

## Adding a New Provider
To add a new provider adapter:
1. Implement the `AIProviderAdapter` interface in `tag.egypt.com.providers`.
2. Register the adapter instance in `ProviderRegistry`.
3. Add the provider constant to `ProviderType`.
4. No modification of `ChatEngine` or UI code is required.
