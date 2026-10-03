package tag.egypt.com.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import tag.egypt.com.model.PromptTemplate

/**
 * Room entity representing a saved reusable prompt template in TAJ EGY.
 */
@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val systemPrompt: String,
    val userTemplate: String,
    val tags: String,
    val isFavorite: Boolean
) {
    fun toDomain(): PromptTemplate {
        return PromptTemplate(id, title, description, systemPrompt, userTemplate, tags, isFavorite)
    }

    companion object {
        fun fromDomain(p: PromptTemplate): PromptEntity {
            return PromptEntity(
                p.id,
                p.title,
                p.description,
                p.systemPrompt,
                p.userTemplate,
                p.tags,
                p.isFavorite
            )
        }
    }
}
