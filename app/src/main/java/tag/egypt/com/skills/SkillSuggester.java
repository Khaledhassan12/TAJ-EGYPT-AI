package tag.egypt.com.skills;

import tag.egypt.com.model.Skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Suggests helpful domain-specific skills when user intent is detected in TAJ EGY.
 */
public final class SkillSuggester {

    public static final class SkillRecommendation {
        private final String title;
        private final String reason;
        private final Skill recommendedSkill;

        public SkillRecommendation(String title, String reason, Skill recommendedSkill) {
            this.title = title;
            this.reason = reason;
            this.recommendedSkill = recommendedSkill;
        }

        public String getTitle() {
            return title;
        }

        public String getReason() {
            return reason;
        }

        public Skill getRecommendedSkill() {
            return recommendedSkill;
        }
    }

    private SkillSuggester() {
        // Utility
    }

    public static List<SkillRecommendation> checkRecommendations(String prompt, List<Skill> installedSkills) {
        if (prompt == null) return Collections.emptyList();
        String lower = prompt.toLowerCase();
        List<SkillRecommendation> recommendations = new ArrayList<>();

        if ((lower.contains("docker") || lower.contains("dockerfile") || lower.contains("container"))
                && !hasSkillWithTag(installedSkills, "docker")) {
            recommendations.add(new SkillRecommendation(
                    "Docker & DevOps Specialist",
                    "Optimizes multi-stage Docker builds, reduces image sizes, and enforces container security best practices.",
                    new Skill(
                            "rec_skill_docker",
                            "Docker & Container Engineering",
                            "1.0.0",
                            "Production Dockerfile and container security guidance.",
                            "When writing Dockerfiles: use specific version tags, multi-stage builds, non-root users, .dockerignore, and minimize layers.",
                            "docker,container,devops,kubernetes",
                            "docker,devops",
                            7,
                            true,
                            Skill.Source.PRESET,
                            "[]"
                    )
            ));
        }

        if ((lower.contains("sql") || lower.contains("query") || lower.contains("database index"))
                && !hasSkillWithTag(installedSkills, "sql")) {
            recommendations.add(new SkillRecommendation(
                    "SQL & Query Optimizer",
                    "Analyzes query execution plans, indexes, normalization, and ACID transaction safety.",
                    new Skill(
                            "rec_skill_sql",
                            "SQL Architecture & Optimization",
                            "1.0.0",
                            "Database design, indexing, and query tuning.",
                            "When assisting with SQL: consider index usage, avoid SELECT *, ensure parameterized queries for SQL injection safety, and explain EXPLAIN output.",
                            "sql,database,postgres,mysql,query,indexing",
                            "sql,database",
                            7,
                            true,
                            Skill.Source.PRESET,
                            "[]"
                    )
            ));
        }

        return Collections.unmodifiableList(recommendations);
    }

    private static boolean hasSkillWithTag(List<Skill> skills, String tag) {
        if (skills == null) return false;
        for (Skill s : skills) {
            if (s.getTags().toLowerCase().contains(tag) || s.getTriggerHints().toLowerCase().contains(tag)) {
                return true;
            }
        }
        return false;
    }
}
