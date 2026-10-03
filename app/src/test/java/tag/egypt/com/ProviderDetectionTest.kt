package tag.egypt.com

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tag.egypt.com.model.ModelCapability
import tag.egypt.com.model.ProviderType
import tag.egypt.com.network.BaseUrlDiscoveryService
import tag.egypt.com.providers.ProviderRegistry

/**
 * Unit tests verifying automatic provider detection and capabilities matrix.
 */
class ProviderDetectionTest {

    @Test
    fun testProviderUrlDetection() {
        val openAiRes = BaseUrlDiscoveryService.discover("https://api.openai.com/v1", null)
        assertEquals(ProviderType.OPENAI, openAiRes.detectedType)
        assertEquals("gpt-4o", openAiRes.recommendedModelId)

        val anthropicRes = BaseUrlDiscoveryService.discover("https://api.anthropic.com", null)
        assertEquals(ProviderType.ANTHROPIC, anthropicRes.detectedType)

        val geminiRes = BaseUrlDiscoveryService.discover("https://generativelanguage.googleapis.com", null)
        assertEquals(ProviderType.GEMINI, geminiRes.detectedType)

        val deepseekRes = BaseUrlDiscoveryService.discover("https://api.deepseek.com", null)
        assertEquals(ProviderType.DEEPSEEK, deepseekRes.detectedType)

        val openRouterRes = BaseUrlDiscoveryService.discover("https://openrouter.ai/api/v1", null)
        assertEquals(ProviderType.OPENROUTER, openRouterRes.detectedType)

        val qwenRes = BaseUrlDiscoveryService.discover("https://dashscope-intl.aliyuncs.com/compatible-mode/v1", null)
        assertEquals(ProviderType.QWEN, qwenRes.detectedType)
    }

    @Test
    fun testProviderRegistryCapabilities() {
        val registry = ProviderRegistry.getInstance()
        val openAiCaps = registry.getCapabilities(ProviderType.OPENAI, "gpt-4o")
        assertTrue(openAiCaps.contains(ModelCapability.VISION))
        assertTrue(openAiCaps.contains(ModelCapability.TOOL_CALLING))
        assertTrue(openAiCaps.contains(ModelCapability.STREAMING))

        val deepseekCaps = registry.getCapabilities(ProviderType.DEEPSEEK, "deepseek-reasoner")
        assertTrue(deepseekCaps.contains(ModelCapability.REASONING_STATUS))
    }
}
