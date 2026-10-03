# Model Context Protocol (MCP) in TAJ EGY

## Overview
TAJ EGY implements the Model Context Protocol (MCP), allowing AI models to interact with external tools and data sources.

## Supported Transports
- **Server-Sent Events (SSE)**: Remote HTTP/HTTPS servers exposing an `/sse` event stream. (Fully Supported)
- **HTTP Streaming**: Direct POST request-response JSON-RPC 2.0 endpoints. (Fully Supported)
- **Stdio**: Desktop local process execution. (Unsupported in Android sandboxed security environment without local shell binaries. The app detects and alerts the user accordingly).

## Security & Approval Gate
Every tool imported from an MCP server has an approval requirement enabled by default. When an AI model generates a tool call, the app displays the tool name, target server, and arguments, allowing the user to approve or cancel the execution.
