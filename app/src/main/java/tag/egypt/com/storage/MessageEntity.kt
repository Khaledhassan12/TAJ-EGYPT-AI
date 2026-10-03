package tag.egypt.com.storage

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import tag.egypt.com.model.ChatMessage

/**
 * Room entity representing an individual chat message in a conversation in TAJ EGY.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val modelUsed: String,
    val providerUsed: String,
    val latencyMs: Long,
    val inputTokens: Int,
    val outputTokens: Int,
    val totalTokens: Int,
    val estimatedCost: Double,
    val status: String,
    val errorMessage: String,
    val attachmentsJson: String,
    val toolCallsJson: String,
    val reasoningSummary: String
) {
    fun toDomain(): ChatMessage {
        val roleEnum = try {
            ChatMessage.Role.valueOf(role)
        } catch (_: Exception) {
            ChatMessage.Role.USER
        }
        val statusEnum = try {
            ChatMessage.Status.valueOf(status)
        } catch (_: Exception) {
            ChatMessage.Status.COMPLETED
        }
        return ChatMessage(
            id,
            conversationId,
            roleEnum,
            content,
            timestamp,
            modelUsed,
            providerUsed,
            latencyMs,
            inputTokens,
            outputTokens,
            totalTokens,
            estimatedCost,
            statusEnum,
            errorMessage,
            emptyList(),
            toolCallsJson,
            reasoningSummary
        )
    }

    companion object {
        fun fromDomain(msg: ChatMessage): MessageEntity {
            return MessageEntity(
                id = msg.id,
                conversationId = msg.conversationId,
                role = msg.role.name,
                content = msg.content,
                timestamp = msg.timestamp,
                modelUsed = msg.modelUsed,
                providerUsed = msg.providerUsed,
                latencyMs = msg.latencyMs,
                inputTokens = msg.inputTokens,
                outputTokens = msg.outputTokens,
                totalTokens = msg.totalTokens,
                estimatedCost = msg.estimatedCost,
                status = msg.status.name,
                errorMessage = msg.errorMessage,
                attachmentsJson = "[]",
                toolCallsJson = msg.toolCallsJson,
                reasoningSummary = msg.reasoningSummary
            )
        }
    }
}
