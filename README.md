# TAJ EGY — Universal Mobile AI Client & Agent for Android

**TAJ EGY** is a production-grade, provider-agnostic Android application for conversational AI and autonomous tool execution. It connects directly to leading AI providers (OpenAI, Anthropic Claude, Google Gemini, DeepSeek, OpenRouter, Alibaba Qwen, and custom OpenAI-compatible proxies) without requiring any intermediary backend server.

---

## 🌟 Key Features

- **Multi-Provider BYOK Architecture**: Add your own credentials for OpenAI, Anthropic, Gemini, DeepSeek, OpenRouter, Qwen, or any custom API.
- **Instant Model Switching**: Switch models and providers on-the-fly without losing conversation history.
- **Real-Time Streaming**: High-speed, character/word streaming with typing animations, interruptible generation, and stop/regenerate capabilities.
- **Model Context Protocol (MCP)**: Native client for MCP servers over SSE and HTTP. Inspect tools and execute them with user permission gates.
- **Declarative AI Skills**: Install domain-specific skills via structured YAML/Markdown or import safe ZIP archives with built-in path-traversal protection.
- **Dynamic Semantic Skill Activation**: Automatically detects user query intent and injects only the most relevant skill into context.
- **Syntax-Highlighted Code Blocks**: Supports 30+ programming languages with word wrap, line numbers, expand/collapse, and one-tap clipboard copy.
- **Mathematical Formula Rendering**: Native formatting for inline and display mathematical equations, Greek letters, fractions, superscripts, and subscripts.
- **Android Keystore AES-256 Security**: Hardware-backed AES-GCM encryption for all stored API keys. No plaintext keys on disk or in logs.
- **Safe Base URL Auto-Discovery**: Probes provider endpoints safely with SSRF and loopback protection.
- **Offline Conversation Vault**: Room SQLite database with full search, pinning, archiving, and JSON export.
- **Bilingual Egyptian Arabic (ar-EG) & American English (en-US)**: Native RTL layout support and natural microcopy.

---

## 🏗️ Project Architecture

TAJ EGY is architected using a Java-heavy foundation for core infrastructure and Kotlin Jetpack Compose for the UI layer:

```
tag.egypt.com/
├── model/           # Universal domain models & capability contracts
├── security/        # Android Keystore encryption & Zip Slip validators
├── storage/         # Room SQLite Database, DAOs, & Entities
├── network/         # OkHttp connection pool, SSE parser, & URL discovery
├── providers/       # Modular provider adapters (OpenAI, Claude, Gemini, DeepSeek, etc.)
├── mcp/             # Model Context Protocol JSON-RPC parser & client
├── skills/          # Skill manager, ZIP extractor, & semantic matcher
├── tools/           # Built-in offline tools (Calculator, Time, Token estimator, JSON)
├── engine/          # ChatEngine orchestrator & streaming pipeline
└── ui/              # Material 3 Expressive Jetpack Compose interface
```

---

## 🚀 Getting Started

1. Clone or open the repository in **Android Studio Meerkat / Ladybug** or newer.
2. Ensure JDK 17 or JDK 21 is selected in Gradle settings.
3. Sync Gradle and build the project (`gradle assembleDebug`).
4. Run on an Android device or emulator running Android 7.0 (API 24) or newer.
5. In the app, navigate to **Providers & Models** and paste your API key to start chatting!
