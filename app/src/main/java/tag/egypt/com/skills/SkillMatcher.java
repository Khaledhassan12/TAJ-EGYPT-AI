package tag.egypt.com.skills;

import tag.egypt.com.model.Skill;

import java.util.List;

/**
 * Intelligent semantic and keyword matcher for installed skills in TAJ EGY.
 */
public final class SkillMatcher {

    private SkillMatcher() {
        // Utility
    }

    public static Skill findBestMatchingSkill(String userPrompt, List<Skill> activeSkills) {
        if (userPrompt == null || activeSkills == null || activeSkills.isEmpty()) {
            return null;
        }

        String lowerPrompt = userPrompt.toLowerCase();
        Skill bestSkill = null;
        int highestScore = 0;

        for (Skill skill : activeSkills) {
            if (!skill.isEnabled()) continue;

            int keywordScore = 0;
            String[] triggers = skill.getTriggerHints().split(",");
            for (String trigger : triggers) {
                String t = trigger.trim().toLowerCase();
                if (!t.isEmpty() && lowerPrompt.contains(t)) {
                    keywordScore += 5;
                }
            }

            String[] tags = skill.getTags().split(",");
            for (String tag : tags) {
                String tg = tag.trim().toLowerCase();
                if (!tg.isEmpty() && lowerPrompt.contains(tg)) {
                    keywordScore += 2;
                }
            }

            if (keywordScore > 0) {
                int totalScore = keywordScore + skill.getPriority();
                if (totalScore > highestScore) {
                    highestScore = totalScore;
                    bestSkill = skill;
                }
            }
        }

        return bestSkill;
    }
}
