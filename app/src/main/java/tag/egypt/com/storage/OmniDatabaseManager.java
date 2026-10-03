package tag.egypt.com.storage;

import android.content.Context;
import android.util.Log;

import tag.egypt.com.model.McpServer;
import tag.egypt.com.model.PromptTemplate;
import tag.egypt.com.model.ProviderConfig;
import tag.egypt.com.model.ProviderType;
import tag.egypt.com.model.Skill;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Data Access Coordinator and Seeder for TAJ EGY.
 */
public final class OmniDatabaseManager {
    private static final String TAG = "TajDbManager";
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

    private OmniDatabaseManager() {
        // Private constructor
    }

    public static void initializeDefaultsAsync(Context context) {
        DB_EXECUTOR.execute(() -> {
            try {
                AppDatabase db = AppDatabase.Companion.getInstance(context);
                ProviderDao providerDao = db.providerDao();
                PromptDao promptDao = db.promptDao();
                SkillDao skillDao = db.skillDao();
                McpDao mcpDao = db.mcpDao();

                if (providerDao.getAllProviders().isEmpty()) {
                    seedDefaultProviders(providerDao);
                }

                if (promptDao.getAllPrompts().isEmpty()) {
                    seedDefaultPrompts(promptDao);
                }

                if (skillDao.getEnabledSkills().isEmpty()) {
                    seedDefaultSkills(skillDao);
                }

                if (mcpDao.getAllServers().isEmpty()) {
                    seedDefaultMcpServers(mcpDao);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing default presets: " + e.getMessage());
            }
        });
    }

    private static void seedDefaultProviders(ProviderDao dao) {
        dao.insertOrUpdate(ProviderEntity.Companion.fromDomain(new ProviderConfig(
                "provider_openai",
                ProviderType.OPENAI,
                "OpenAI",
                "",
                "https://api.openai.com/v1",
                "gpt-4o",
                true,
                true,
                0.7f,
                1.0f,
                4096,
                true,
                "You are TAJ EGY, an expert, concise, and helpful AI assistant.",
                60,
                ""
        )));

        dao.insertOrUpdate(ProviderEntity.Companion.fromDomain(new ProviderConfig(
                "provider_anthropic",
                ProviderType.ANTHROPIC,
                "Anthropic Claude",
                "",
                "https://api.anthropic.com/v1",
                "claude-3-5-sonnet-20241022",
                true,
                false,
                0.7f,
                1.0f,
                4096,
                true,
                "You are Claude, an AI created by Anthropic to be helpful, harmless, and honest.",
                60,
                ""
        )));

        dao.insertOrUpdate(ProviderEntity.Companion.fromDomain(new ProviderConfig(
                "provider_gemini",
                ProviderType.GEMINI,
                "Google Gemini",
                "",
                "https://generativelanguage.googleapis.com",
                "gemini-1.5-pro",
                true,
                false,
                0.7f,
                1.0f,
                8192,
                true,
                "You are a helpful AI assistant powered by Gemini.",
                60,
                ""
        )));

        dao.insertOrUpdate(ProviderEntity.Companion.fromDomain(new ProviderConfig(
                "provider_deepseek",
                ProviderType.DEEPSEEK,
                "DeepSeek",
                "",
                "https://api.deepseek.com",
                "deepseek-chat",
                true,
                false,
                0.7f,
                1.0f,
                4096,
                true,
                "You are DeepSeek, an AI assistant focused on deep technical reasoning and coding.",
                60,
                ""
        )));

        dao.insertOrUpdate(ProviderEntity.Companion.fromDomain(new ProviderConfig(
                "provider_openrouter",
                ProviderType.OPENROUTER,
                "OpenRouter",
                "",
                "https://openrouter.ai/api/v1",
                "meta-llama/llama-3.3-70b-instruct",
                true,
                false,
                0.7f,
                1.0f,
                4096,
                true,
                "",
                60,
                ""
        )));

        dao.insertOrUpdate(ProviderEntity.Companion.fromDomain(new ProviderConfig(
                "provider_qwen",
                ProviderType.QWEN,
                "Qwen (Alibaba Cloud)",
                "",
                "https://dashscope-intl.aliyuncs.com/compatible-mode/v1",
                "qwen-plus",
                true,
                false,
                0.7f,
                1.0f,
                4096,
                true,
                "",
                60,
                ""
        )));
    }

    private static void seedDefaultPrompts(PromptDao dao) {
        dao.insertOrUpdate(PromptEntity.Companion.fromDomain(new PromptTemplate(
                "prompt_code_reviewer",
                "Expert Code Reviewer",
                "Performs deep code review focusing on security, performance, and best practices.",
                "You are a principal software engineer. Review code thoroughly, identify bugs, security vulnerabilities, and architectural pitfalls.",
                "Review the following {{language}} code for {{project}}:\n\n```{{language}}\n{{code}}\n```\n\nIdentify bottlenecks and recommend production improvements.",
                "coding,review,security",
                true
        )));

        dao.insertOrUpdate(PromptEntity.Companion.fromDomain(new PromptTemplate(
                "prompt_summarizer",
                "Executive Summary",
                "Condenses long articles or documents into actionable executive bullet points.",
                "You are an executive assistant who summarizes complex materials with high precision and clarity.",
                "Please summarize the following text into 3 key takeaways and actionable next steps:\n\n{{text}}",
                "productivity,summary",
                true
        )));

        dao.insertOrUpdate(PromptEntity.Companion.fromDomain(new PromptTemplate(
                "prompt_translator",
                "Bilingual Translator (EN <-> AR)",
                "Translates between English and Egyptian Arabic with natural conversational idioms.",
                "You are an expert bilingual linguist specialized in fluent, natural English and Arabic (Egyptian dialect / Modern Standard Arabic).",
                "Translate the following text into {{target_language}} with culturally nuanced phrasing:\n\n{{text}}",
                "language,arabic,translation",
                false
        )));
    }

    private static void seedDefaultSkills(SkillDao dao) {
        dao.insertOrUpdate(SkillEntity.Companion.fromDomain(new Skill(
                "skill_android_expert",
                "Android Architecture & Jetpack Compose",
                "1.0.0",
                "Provides advanced guidelines for modern Jetpack Compose, Kotlin Coroutines, and M3 design tokens.",
                "When writing Android code: prioritize Material 3 Expressive, remember to use remember and derivedStateOf, keep view models clean with StateFlow, handle edge-to-edge window insets, and ensure accessibility touch targets are >= 48dp.",
                "android,compose,kotlin,jetpack,lifecycle,ui",
                "android,mobile,kotlin",
                10,
                true,
                Skill.Source.PRESET,
                "[]"
        )));

        dao.insertOrUpdate(SkillEntity.Companion.fromDomain(new Skill(
                "skill_security_auditor",
                "Application Security Auditor",
                "1.0.0",
                "Specialized in identifying OWASP vulnerabilities, SSRF, injection, and credential leak risks.",
                "Always inspect user input validation, SQL/command injection avenues, path traversal vulnerabilities (Zip Slip), cryptographic key safety, and API credential storage.",
                "security,audit,owasp,vulnerability,cve,encryption",
                "security,backend,cryptography",
                8,
                true,
                Skill.Source.PRESET,
                "[]"
        )));
    }

    private static void seedDefaultMcpServers(McpDao dao) {
        dao.insertOrUpdate(McpServerEntity.Companion.fromDomain(new McpServer(
                "mcp_demo_dev",
                "Developer Tools MCP",
                "https://mcp.example.com/sse",
                McpServer.Transport.SSE,
                "Remote MCP server providing code analysis, git status checks, and unit test execution.",
                false,
                false,
                "",
                "{}",
                4
        )));
    }
}
