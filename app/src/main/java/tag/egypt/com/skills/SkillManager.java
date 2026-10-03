package tag.egypt.com.skills;

import android.content.Context;
import android.util.Log;

import tag.egypt.com.model.Skill;
import tag.egypt.com.security.SecurityValidator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Manages Skill creation, safe ZIP import validation, and disk management in TAJ EGY.
 */
public final class SkillManager {
    private static final String TAG = "TajSkillManager";
    private static final long MAX_UNCOMPRESSED_SIZE_BYTES = 50 * 1024 * 1024; // 50MB
    private static final int MAX_ZIP_ENTRIES = 200;

    private SkillManager() {
        // Utility
    }

    public static Skill importSkillZip(Context context, InputStream zipStream) throws Exception {
        File skillsDir = new File(context.getFilesDir(), "installed_skills");
        if (!skillsDir.exists() && !skillsDir.mkdirs()) {
            throw new IllegalStateException("Failed to create skills directory");
        }

        String skillId = "skill_zip_" + System.currentTimeMillis();
        File targetDir = new File(skillsDir, skillId);
        if (!targetDir.mkdirs()) {
            throw new IllegalStateException("Failed to create target skill folder");
        }

        long totalSize = 0;
        int totalEntries = 0;
        String skillName = "Imported Skill";
        String skillDescription = "Imported from ZIP archive";
        StringBuilder instructions = new StringBuilder();

        try (ZipInputStream zis = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = zis.getNextEntry()) != null) {
                totalEntries++;
                if (totalEntries > MAX_ZIP_ENTRIES) {
                    throw new SecurityException("Archive contains too many files (max " + MAX_ZIP_ENTRIES + ")");
                }

                File targetFile = SecurityValidator.validateZipEntry(targetDir, entry);

                if (entry.isDirectory()) {
                    if (!targetFile.exists() && !targetFile.mkdirs()) {
                        throw new IllegalStateException("Failed to create directory: " + targetFile.getName());
                    }
                    continue;
                }

                File parent = targetFile.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Failed to create parent directory for: " + targetFile.getName());
                }

                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        totalSize += len;
                        if (totalSize > MAX_UNCOMPRESSED_SIZE_BYTES) {
                            throw new SecurityException("Archive uncompressed size exceeds maximum 50MB safety limit (potential Zip bomb)");
                        }
                        fos.write(buffer, 0, len);
                    }
                }

                if (entry.getName().endsWith("SKILL.md") || entry.getName().endsWith("skill.md")) {
                    parseSkillFile(targetFile, instructions);
                    skillName = targetFile.getParentFile() != null ? targetFile.getParentFile().getName() : "Imported Skill";
                }
            }
        }

        if (instructions.length() == 0) {
            instructions.append("Custom declarative skill instructions imported from archive.");
        }

        return new Skill(
                skillId,
                skillName,
                "1.0.0",
                skillDescription,
                instructions.toString(),
                "imported,custom",
                "custom",
                5,
                true,
                Skill.Source.IMPORTED_ZIP,
                "[]"
        );
    }

    private static void parseSkillFile(File file, StringBuilder outInstructions) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outInstructions.append(line).append("\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed reading SKILL.md: " + e.getMessage());
        }
    }
}
