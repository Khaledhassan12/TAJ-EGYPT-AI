# TAJ EGY — Architectural Specification

## 1. Architectural Philosophy
The core tenet of TAJ EGY is **provider-agnostic independence**. The client does not depend on a proprietary backend server; all requests originate directly from the mobile device to the respective provider's public API using client-provided credentials.

## 2. Layered Structure
- **Presentation Layer (`tag.egypt.com.ui`)**: Built with Jetpack Compose using Material 3 Expressive guidelines. Consists of composable screens, custom text renderers, code highlighters, and ViewModels exposing immutable `StateFlow` streams.
- **Engine Layer (`tag.egypt.com.engine`)**: Coordinates conversation history retrieval, active provider adapter selection, skill injection, token calculation, latency metrics, and Room database persistence.
- **Provider Abstraction Layer (`tag.egypt.com.providers`)**: Defines `AIProviderAdapter` and `ProviderRegistry`. Concrete adapters translate canonical domain requests to provider-specific wire protocols (OpenAI, Claude, Gemini, DeepSeek, etc.).
- **Protocol & Integration Layer (`tag.egypt.com.mcp`, `tag.egypt.com.skills`, `tag.egypt.com.tools`)**: Extensible subsystems for external Model Context Protocol tool servers, declarative Markdown skills, and built-in offline calculators and parsers.
- **Persistence & Security Layer (`tag.egypt.com.storage`, `tag.egypt.com.security`)**: SQLite Room database and hardware-backed Android KeyStore AES-256 GCM cryptographic vault.

## 3. Threading Model
- **UI Thread**: Exclusively handles Compose rendering and user input gestures.
- **Background Dispatchers**: Network calls run on OkHttp's asynchronous thread pool. Database queries run on a dedicated single-thread executor. Heavy parsing runs off the main thread to ensure 60fps streaming animations.
