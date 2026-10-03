# Troubleshooting & FAQs

### 1. HTTP 401 / Unauthorized Error
- Verify that your API key is correctly entered in **Providers & Models** for that service.
- Check that the model specified is accessible on your provider account tier.

### 2. Base URL Auto-Detection Warnings
- If you enter a private LAN IP (e.g. `192.168.x.x` or `10.x.x.x`), the app flags that the host is on a local/private network. Ensure your Android device and local LLM host (e.g. Ollama) are connected to the same Wi-Fi network.

### 3. MCP Server Connections
- Ensure the MCP server supports HTTP or SSE endpoints. Android sandbox security prevents local desktop `stdio` command execution.
