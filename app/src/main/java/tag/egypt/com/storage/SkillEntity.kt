package tag.egypt.com.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import tag.egypt.com.model.Skill

/**
 * Room entity representing an installed AI Skill in TAJ EGY.
 */
@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val instructions: String,
    val triggerHints: String,
    val tags: String,
    val priority: Int,
    val isEnabled: Boolean,
    val source: String,
    val filesSummaryJson: String
) {
    fun toDomain(): Skill {
        val src = try {
            Skill.Source.valueOf(source)
        } catch (_: Exception) {
            Skill.Source.MANUAL
        }
        return Skill(
            id,
            name,
            version,
            description,
            instructions,
            triggerHints,
            tags,
            priority,
            isEnabled,
            src,
            filesSummaryJson
        )
    }

    companion object {
        fun fromDomain(s: Skill): SkillEntity {
            return SkillEntity(
                s.id,
                s.name,
                s.version,
                s.description,
                s.instructions,
                s.triggerHints,
                s.tags,
                s.priority,
                s.isEnabled,
                s.source.name,
                s.filesSummaryJson
            )
        }
    }
}
