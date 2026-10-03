package tag.egypt.com

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import tag.egypt.com.model.Skill
import tag.egypt.com.skills.SkillMatcher

/**
 * Unit tests verifying semantic skill matching and relevance scoring.
 */
class SkillMatcherTest {

    @Test
    fun testFindBestMatchingSkill() {
        val skills = listOf(
            Skill(
                "skill_android",
                "Android Compose",
                "1.0.0",
                "Android Compose expert",
                "Use remember and derivedStateOf",
                "jetpack compose,android,kotlin",
                "mobile,android",
                10,
                true,
                Skill.Source.PRESET,
                "[]"
            ),
            Skill(
                "skill_docker",
                "Docker Specialist",
                "1.0.0",
                "Docker expert",
                "Multi-stage Docker builds",
                "docker,container,kubernetes",
                "devops",
                5,
                true,
                Skill.Source.PRESET,
                "[]"
            )
        )

        val matchedAndroid = SkillMatcher.findBestMatchingSkill(
            "How do I optimize Jetpack Compose recomposition in my Android app?",
            skills
        )
        assertNotNull(matchedAndroid)
        assertEquals("skill_android", matchedAndroid?.id)

        val matchedDocker = SkillMatcher.findBestMatchingSkill(
            "Write a multi-stage Docker build for a Golang backend",
            skills
        )
        assertNotNull(matchedDocker)
        assertEquals("skill_docker", matchedDocker?.id)

        val noMatch = SkillMatcher.findBestMatchingSkill(
            "What is the capital of France?",
            skills
        )
        assertNull(noMatch)
    }
}
