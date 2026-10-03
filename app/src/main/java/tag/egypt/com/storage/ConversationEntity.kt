package tag.egypt.com.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import tag.egypt.com.model.Conversation

/**
 * Room entity representing a saved conversation session in SQLite in TAJ EGY.
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val providerId: String,
    val modelId: String,
    val systemPrompt: String,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val messageCount: Int
) {
    fun toDomain(): Conversation {
        return Conversation(
            id,
            title,
            createdAt,
            updatedAt,
            providerId,
            modelId,
            systemPrompt,
            isPinned,
            isArchived,
            messageCount
        )
    }

    companion object {
        fun fromDomain(conv: Conversation): ConversationEntity {
            return ConversationEntity(
                id = conv.id,
                title = conv.title,
                createdAt = conv.createdAt,
                updatedAt = conv.updatedAt,
                providerId = conv.providerId,
                modelId = conv.modelId,
                systemPrompt = conv.systemPrompt,
                isPinned = conv.isPinned,
                isArchived = conv.isArchived,
                messageCount = conv.messageCount
            )
        }
    }
}
