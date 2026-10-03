package tag.egypt.com.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Android Keystore-backed cryptographic store for user API keys in TAJ EGY.
 */
public final class SecureCredentialStore {
    private static final String TAG = "TajSecurity";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "TajEgyMasterKey_v1";
    private static final String PREFS_NAME = "taj_egy_vault";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;

    private static volatile SecureCredentialStore instance;

    private final SharedPreferences preferences;
    private final Object lock = new Object();

    private SecureCredentialStore(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ensureMasterKeyExists();
    }

    public static SecureCredentialStore getInstance(Context context) {
        if (instance == null) {
            synchronized (SecureCredentialStore.class) {
                if (instance == null) {
                    instance = new SecureCredentialStore(context);
                }
            }
        }
        return instance;
    }

    private void ensureMasterKeyExists() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        ANDROID_KEY_STORE
                );
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
                )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .setRandomizedEncryptionRequired(true)
                        .build();

                keyGenerator.init(spec);
                keyGenerator.generateKey();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed initializing Android Keystore master key: " + e.getMessage());
        }
    }

    private SecretKey getMasterKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
    }

    public boolean saveApiKey(String providerId, String rawApiKey) {
        if (providerId == null) return false;
        if (rawApiKey == null || rawApiKey.trim().isEmpty()) {
            deleteApiKey(providerId);
            return true;
        }

        synchronized (lock) {
            try {
                SecretKey secretKey = getMasterKey();
                if (secretKey == null) {
                    ensureMasterKeyExists();
                    secretKey = getMasterKey();
                }

                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] iv = cipher.getIV();
                byte[] cipherText = cipher.doFinal(rawApiKey.getBytes(StandardCharsets.UTF_8));

                byte[] combined = new byte[iv.length + cipherText.length];
                System.arraycopy(iv, 0, combined, 0, iv.length);
                System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

                String encoded = Base64.encodeToString(combined, Base64.NO_WRAP);
                preferences.edit().putString("key_" + providerId, encoded).apply();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error encrypting credential for provider: " + providerId, e);
                return false;
            }
        }
    }

    public String getApiKey(String providerId) {
        if (providerId == null) return "";
        synchronized (lock) {
            try {
                String encoded = preferences.getString("key_" + providerId, null);
                if (encoded == null || encoded.isEmpty()) {
                    return "";
                }

                byte[] combined = Base64.decode(encoded, Base64.NO_WRAP);
                if (combined.length <= GCM_IV_LENGTH_BYTES) {
                    return "";
                }

                byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
                System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH_BYTES);

                int cipherLength = combined.length - GCM_IV_LENGTH_BYTES;
                byte[] cipherText = new byte[cipherLength];
                System.arraycopy(combined, GCM_IV_LENGTH_BYTES, cipherText, 0, cipherLength);

                SecretKey secretKey = getMasterKey();
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

                byte[] plainBytes = cipher.doFinal(cipherText);
                return new String(plainBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                Log.e(TAG, "Error decrypting credential for provider: " + providerId, e);
                return "";
            }
        }
    }

    public void deleteApiKey(String providerId) {
        if (providerId == null) return;
        synchronized (lock) {
            preferences.edit().remove("key_" + providerId).apply();
        }
    }

    public void clearAllCredentials() {
        synchronized (lock) {
            preferences.edit().clear().apply();
        }
    }

    public static String maskKey(String rawKey) {
        if (rawKey == null || rawKey.isEmpty()) {
            return "No key configured";
        }
        if (rawKey.length() <= 4) {
            return "••••";
        }
        String lastFour = rawKey.substring(rawKey.length() - 4);
        return "••••••••••••" + lastFour;
    }
}
