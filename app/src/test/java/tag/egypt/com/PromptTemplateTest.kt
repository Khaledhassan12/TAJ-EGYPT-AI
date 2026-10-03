package tag.egypt.com

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tag.egypt.com.model.PromptTemplate

/**
 * Unit tests verifying prompt variable placeholder extraction and substitution.
 */
class PromptTemplateTest {

    @Test
    fun testVariableExtraction() {
        val template = PromptTemplate(
            "test_prompt",
            "Code Review",
            "Desc",
            "",
            "Review this {{language}} code for project {{project}}. Focus on {{task}}.",
            "test",
            false
        )

        val vars = template.extractVariables()
        assertEquals(3, vars.size)
        assertTrue(vars.contains("language"))
        assertTrue(vars.contains("project"))
        assertTrue(vars.contains("task"))
    }

    @Test
    fun testVariableRendering() {
        val template = PromptTemplate(
            "test_prompt",
            "Greeting",
            "Desc",
            "",
            "Hello, {{name}}! Welcome to {{app}}.",
            "test",
            false
        )

        val values = mapOf(
            "name" to "Khaled",
            "app" to "TAJ EGY"
        )

        val rendered = template.render(values)
        assertEquals("Hello, Khaled! Welcome to TAJ EGY.", rendered)
    }
}
