# Performance Optimization & Battery Awareness

## 1. Network Pooling & HTTP/2
All provider adapters and MCP tool clients share a singleton `OkHttpClient` instance configured with connection pooling (5 idle connections, 5-minute keep-alive) and HTTP/2 multiplexing. This prevents socket exhaustion during high-frequency conversational streams.

## 2. Low-Memory & Reduced Motion Mode
In **Settings**, users can toggle Low-Memory Mode to throttle image decoding cache and disable heavy typing animations, maintaining 60fps responsiveness even on budget hardware.

## 3. SQLite Room Indexing
Conversations and message histories are indexed by `conversationId` and `timestamp`, allowing sub-millisecond retrieval times without full table scans.
