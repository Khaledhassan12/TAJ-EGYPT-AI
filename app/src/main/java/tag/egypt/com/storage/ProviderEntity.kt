package tag.egypt.com.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import tag.egypt.com.model.ProviderConfig
import tag.egypt.com.model.ProviderType

/**
 * Room entity representing configured AI provider metadata in TAJ EGY.
 */
@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey
    val id: String,
    val providerType: String,
    val name: String,
    val baseUrl: String,
    val defaultModelId: String,
    val isEnabled: Boolean,
    val isDefault: Boolean,
    val temperature: Float,
    val topP: Float,
    val maxTokens: Int,
    val streamEnabled: Boolean,
    val systemPrompt: String,
    val timeoutSeconds: Int,
    val organizationId: String
) {
    fun toDomain(apiKey: String): ProviderConfig {
        val type = try {
            ProviderType.valueOf(providerType)
        } catch (_: Exception) {
            ProviderType.OPENAI
        }
        return ProviderConfig(
            id,
            type,
            name,
            apiKey,
            baseUrl,
            defaultModelId,
            isEnabled,
            isDefault,
            temperature,
            topP,
            maxTokens,
            streamEnabled,
            systemPrompt,
            timeoutSeconds,
            organizationId
        )
    }

    companion object {
        fun fromDomain(config: ProviderConfig): ProviderEntity {
            return ProviderEntity(
                id = config.id,
                providerType = config.providerType.name,
                name = config.name,
                baseUrl = config.baseUrl,
                defaultModelId = config.defaultModelId,
                isEnabled = config.isEnabled,
                isDefault = config.isDefault,
                temperature = config.temperature,
                topP = config.topP,
                maxTokens = config.maxTokens,
                streamEnabled = config.isStreamEnabled,
                systemPrompt = config.systemPrompt,
                timeoutSeconds = config.timeoutSeconds,
                organizationId = config.organizationId
            )
        }
    }
}
