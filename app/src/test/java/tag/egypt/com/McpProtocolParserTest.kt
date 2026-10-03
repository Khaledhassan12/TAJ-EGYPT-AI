package tag.egypt.com

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tag.egypt.com.mcp.McpProtocolParser
import tag.egypt.com.model.McpServer

/**
 * Unit tests verifying Model Context Protocol (MCP) JSON configuration parsing.
 */
@RunWith(RobolectricTestRunner::class)
class McpProtocolParserTest {

    @Test
    fun testParseStandardMcpConfigJson() {
        val json = """
            {
              "mcpServers": {
                "weather-server": {
                  "url": "https://mcp.weather.com/sse",
                  "description": "Live weather reporting tool"
                },
                "desktop-runner": {
                  "command": "npx",
                  "args": ["-y", "@modelcontextprotocol/server-sqlite"]
                }
              }
            }
        """.trimIndent()

        val servers = McpProtocolParser.parseMcpConfigJson(json)
        assertEquals(2, servers.size)

        val weatherServer = servers.first { it.name == "weather-server" }
        assertEquals("https://mcp.weather.com/sse", weatherServer.serverUrl)
        assertEquals(McpServer.Transport.SSE, weatherServer.transport)
        assertTrue(weatherServer.isEnabled)

        val desktopServer = servers.first { it.name == "desktop-runner" }
        assertEquals(McpServer.Transport.STDIO, desktopServer.transport)
        assertFalse("STDIO transport must not be enabled by default on Android sandbox", desktopServer.isEnabled)
    }

    @Test
    fun testParseToolsListRpc() {
        val rpc = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "result": {
                "tools": [
                  {
                    "name": "get_forecast",
                    "description": "Fetches 5-day weather forecast",
                    "inputSchema": {
                      "type": "object",
                      "properties": { "city": { "type": "string" } }
                    }
                  }
                ]
              }
            }
        """.trimIndent()

        val tools = McpProtocolParser.parseToolsList("server_123", rpc)
        assertEquals(1, tools.size)
        assertEquals("get_forecast", tools[0].name)
        assertTrue(tools[0].inputSchemaJson.contains("city"))
    }
}
