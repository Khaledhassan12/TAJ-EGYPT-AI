package tag.egypt.com.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import tag.egypt.com.model.McpServer

/**
 * Room entity representing an external MCP server configuration in TAJ EGY.
 */
@Entity(tableName = "mcp_servers")
data class McpServerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val serverUrl: String,
    val transport: String,
    val description: String,
    val isEnabled: Boolean,
    val isAutoConnect: Boolean,
    val authToken: String,
    val customHeadersJson: String,
    val toolCount: Int
) {
    fun toDomain(): McpServer {
        val transportEnum = try {
            McpServer.Transport.valueOf(transport)
        } catch (_: Exception) {
            McpServer.Transport.SSE
        }
        return McpServer(
            id,
            name,
            serverUrl,
            transportEnum,
            description,
            isEnabled,
            isAutoConnect,
            authToken,
            customHeadersJson,
            toolCount
        )
    }

    companion object {
        fun fromDomain(m: McpServer): McpServerEntity {
            return McpServerEntity(
                m.id,
                m.name,
                m.serverUrl,
                m.transport.name,
                m.description,
                m.isEnabled,
                m.isAutoConnect,
                m.authToken,
                m.customHeadersJson,
                m.toolCount
            )
        }
    }
}
